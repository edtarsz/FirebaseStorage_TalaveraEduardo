package mx.edu.itson.practica12

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class PokemonAdapter(context: Context, private val pokemonList: MutableList<Pokemon>) :
    ArrayAdapter<Pokemon>(context, 0, pokemonList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)

        val pokemon = getItem(position)

        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)

        text1.text = "N° ${pokemon?.number}"
        text2.text = pokemon?.name

        return view
    }

    // Método para agregar un nuevo Pokémon y actualizar el ListView
    fun agregarPokemon(pokemon: Pokemon) {
        pokemonList.add(pokemon)
        notifyDataSetChanged()  // Notifica al adaptador para actualizar la vista
    }
}

