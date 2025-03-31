package mx.edu.itson.practica12

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Eduardo Talavera Ramos 31/03/2025
class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        adapter = PokemonAdapter(this, pokemonList)
        listView.adapter = adapter

        val button: Button = findViewById(R.id.btnRegistrarPkm)
        button.setOnClickListener {
            val intent = Intent(this, RegistrarPokemon::class.java)
            startActivity(intent)
        }

        database = FirebaseDatabase.getInstance().reference.child("pokemons")

        cargarPokemones()
    }

    override fun onResume() {
        super.onResume()
        cargarPokemones()
    }

    private fun cargarPokemones() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pokemonList.clear()

                for (pokemonSnapshot in snapshot.children) {
                    val pokemon = pokemonSnapshot.getValue(Pokemon::class.java)
                    if (pokemon != null) {
                        pokemonList.add(pokemon)
                        Log.d("MainActivity", "Pokémon encontrado: ${pokemon.name}")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Mejora el manejo de errores
                Log.e("MainActivity", "Error al cargar datos: ${error.message}")
            }
        })
    }
}