package mx.edu.itson.practica12

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

// Eduardo Talavera Ramos 31/03/2025
class PokemonAdapter(
    private val context: Context, private val pokemones: ArrayList<Pokemon>
) : BaseAdapter() {

    override fun getCount(): Int {
        return pokemones.size
    }

    override fun getItem(position: Int): Pokemon {
        return pokemones[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_pokemon, parent, false).apply {
                tag = SetearDatos(this)
        }

        val holder = view.tag as SetearDatos
        val pokemon = getItem(position)

        holder.nombreTextView.text = pokemon.name
        holder.numeroTextView.text = pokemon.number.toString()

        // Está medio naco esto profe, pero si no lo hacía no me encontraba las fotos
        // Como 2 horas buscando el error que horrible
        // Corrige la url antes de cargar la información
        val correctedUrl = pokemon.imageUrl
            ?.replace("http://", "https://")
            ?.replace("cloudmary.com", "cloudinary.com")

        // Glide es para renderizar imagenes de una forma muy simple la vdd
        Glide.with(context)
            .load(correctedUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imagenImageView)

        return view
    }

    private class SetearDatos(view: View) {
        val imagenImageView: ImageView = view.findViewById(R.id.imgPokemon)
        val nombreTextView: TextView = view.findViewById(R.id.nombrePokemon)
        val numeroTextView: TextView = view.findViewById(R.id.numeroPokemon)
    }
}