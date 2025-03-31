package mx.edu.itson.practica12

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// Eduardo Talavera Ramos 31/03/2025
class RegistrarPokemon : AppCompatActivity() {
    // Código de solicitud para la selección de imágenes
    private val REQUEST_IMAGE_GET = 1

    // Configuración de Cloudinary
    private val CLOUD_NAME = "dylhe0h1o"
    private val UPLOAD_PRESET = "pokemon-upload"

    // URI de la imagen seleccionada y URL pública después de subirla
    private var imageUri: Uri? = null
    private var imagePublicUrl: String? = null

    private lateinit var name: EditText
    private lateinit var number: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var thumbnail: ImageView
    private lateinit var btnSave: Button

    // Referencias a Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var pokemonRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_pokemon)

        // Inicializa Cloudinary
        initCloudinary()

        // Configuracion del Firebase Database
        database = FirebaseDatabase.getInstance()
        pokemonRef = database.getReference("pokemons")

        name = findViewById(R.id.etName)
        number = findViewById(R.id.etNumber)
        btnUploadImage = findViewById(R.id.btnSubirImg)
        btnSave = findViewById(R.id.btnSavePokemon)
        thumbnail = findViewById(R.id.imageView)

        // listener para el botón de subir imagen
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }

        // listener para el botón de guardar
        btnSave.setOnClickListener {
            val nameText = name.text.toString()
            val numberText = number.text.toString().toIntOrNull()

            // Validaciones
            if (nameText.isEmpty() || numberText == null) {
                Toast.makeText(this, "Ingresa datos válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si hay una imagen seleccionada, la sube a Cloudinary
            if (imageUri != null) {
                uploadImageToCloudinary()
            } else {
                // Si no hay imagen no sube nada
            }
        }
    }

    // Inicializa la configuración de Cloudinary.
    private fun initCloudinary() {
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = CLOUD_NAME
        try {
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            Log.e("Cloudinary", "Error al inicializar Cloudinary", e)
        }
    }

    // Maneja el resultado de la selección de imagen.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                changeImage(uri)
                imageUri = uri
            }
        }
    }

    // Muestra la imagen seleccionada en el ImageView.
    private fun changeImage(uri: Uri) {
        try {
            thumbnail.setImageURI(uri)
        } catch (e: Exception) {
            Log.e("ImageError", "Error al cargar la imagen", e)
        }
    }

    // Sube la imagen seleccionada a Cloudinary.
    private fun uploadImageToCloudinary() {
        imageUri?.let { uri ->
            MediaManager.get().upload(uri).unsigned(UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.i("Cloudinary", "Comenzando subida de imagen")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.i("Cloudinary", "Subiendo imagen... ${bytes * 100 / totalBytes}%")
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("Cloudinary", "Error al subir imagen: ${error?.description}")
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.i("Cloudinary", "Subida reprogramada")
                    }

                    override fun onSuccess(
                        requestId: String?, resultData: MutableMap<Any?, Any?>?
                    ) {
                        imagePublicUrl = resultData?.get("url") as String?
                        Log.i("Cloudinary", "Imagen subida: $imagePublicUrl")
                        savePokemonToFirebase(imagePublicUrl)
                    }
                }).dispatch()
        }
    }

    // Guarda los datos del Pokémon en Firebase Database.
    private fun savePokemonToFirebase(imageUrl: String?) {
        val nameText = name.text.toString()
        val numberText = number.text.toString().toIntOrNull()

        // Verifica que los datos sean válidos antes de guardar
        if (nameText.isNotEmpty() && numberText != null) {
            val pokemon = Pokemon(numberText, nameText, imageUrl)

            // Genera una clave única en Firebase
            val key = pokemonRef.push().key

            // El let se supone que ejecuta un codigo solo si la variable no es nula, en este caso el key (la referencia)
            key?.let {
                // Esto es para meter dentro del pokemones el pokemon
                pokemonRef.child(it).setValue(pokemon).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}