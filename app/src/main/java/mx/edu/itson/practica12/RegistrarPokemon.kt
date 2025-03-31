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
import javax.security.auth.callback.Callback

class RegistrarPokemon : AppCompatActivity() {
    val REQUEST_IMAGE_GET = 1

    val CLOUD_NAME = "dylhe0h1o"
    val UPLOAD_PRESET = "pokemon-upload"

    var imageUri: Uri? = null
    var imagePublicUrl: String? = null

    lateinit var name: EditText
    lateinit var number: EditText
    lateinit var btnUploadImage: Button
    lateinit var thumbnail: ImageView
    lateinit var btnSave: Button

    private lateinit var database: FirebaseDatabase
    private lateinit var pokemonRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_pokemon)

        initCloudinary()

        database = FirebaseDatabase.getInstance()
        pokemonRef = database.getReference("pokemons")

        name = findViewById(R.id.etName)
        number = findViewById(R.id.etNumber)
        btnUploadImage = findViewById(R.id.btnSubirImg)
        btnSave = findViewById(R.id.btnSavePokemon)
        thumbnail = findViewById(R.id.imageView)

        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }

        btnSave.setOnClickListener {
            if (imageUri != null) {
                uploadImageToCloudinary() // Primero subimos la imagen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                savePokemonToFirebase(null) // Guardamos sin imagen si no hay
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun initCloudinary() {
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = CLOUD_NAME
        try {
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            // Ya estaba inicializado, ignoramos el error
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val fullPhotoUrl: Uri? = data?.data
            if (fullPhotoUrl != null) {
                changeImage(fullPhotoUrl)
                imageUri = fullPhotoUrl
            }
        }
    }

    private fun changeImage(uri: Uri) {
        try {
            thumbnail.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uploadImageToCloudinary() {
        imageUri?.let { uri ->
            MediaManager.get().upload(uri).unsigned(UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.i("OnStart", "Subida iniciada")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.i("onProgress", "Subiendo...")
                    }

                    override fun onSuccess(
                        requestId: String?, resultData: MutableMap<Any?, Any?>?
                    ) {
                        imagePublicUrl = resultData?.get("url") as String?
                        savePokemonToFirebase(imagePublicUrl) // Guardar en Firebase solo después de subir la imagen
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("onError", "Error al subir: ${error.toString()}")
                        savePokemonToFirebase(null) // Guardar sin imagen si hay error
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.i("onReschedule", "Reprogramado")
                    }
                }).dispatch()
        }
    }

    fun savePokemonToFirebase(imageUrl: String?) {
        val nameText = name.text.toString()
        val numberText = number.text.toString().toIntOrNull()

        if (nameText.isNotEmpty() && numberText != null) {
            // Create Pokemon with the image URL included
            val pokemon = Pokemon(numberText, nameText, imageUrl)

            val key = pokemonRef.push().key
            if (key != null) {
                pokemonRef.child(key).setValue(pokemon).addOnSuccessListener {
                    Toast.makeText(this, "Pokémon guardado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Ingresa datos válidos", Toast.LENGTH_SHORT).show()
        }
    }
}