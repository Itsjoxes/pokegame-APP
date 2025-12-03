package com.example.pokegame.model

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokegame.R
import com.example.pokegame.api.Pokemon
import com.example.pokegame.databinding.ItemCaptureBinding
import com.example.pokegame.util.RarityBorderMapper
import com.example.pokegame.util.TypeColorMapper

class CaptureAdapter(private val onCaptureClick: (Pokemon) -> Unit) :
        ListAdapter<Pokemon, CaptureAdapter.CaptureViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptureViewHolder {
        val binding = ItemCaptureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaptureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaptureViewHolder, position: Int) {
        val pokemon = getItem(position)
        holder.bind(pokemon, onCaptureClick)
    }

    class CaptureViewHolder(private val binding: ItemCaptureBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(pokemon: Pokemon, onCaptureClick: (Pokemon) -> Unit) {
            binding.pokemonName.text = pokemon.name.uppercase()
            binding.pokemonName.setTypeface(null, Typeface.BOLD)

            // Set rarity border
            val borderColor = RarityBorderMapper.getBorderColorForRarity(pokemon)
            binding.card.strokeColor = ContextCompat.getColor(itemView.context, borderColor)

            // Add type labels
            binding.typesContainer.removeAllViews()
            pokemon.types.forEach { pokemonType ->
                val typeTextView =
                        TextView(itemView.context).apply {
                            text = pokemonType.type.name.take(3).uppercase()

                            val typeface = ResourcesCompat.getFont(context, R.font.jersey_10)
                            setTypeface(typeface, Typeface.BOLD)

                            setTextColor(Color.WHITE)
                            letterSpacing = 0.1f

                            val backgroundDrawable =
                                    GradientDrawable().apply {
                                        shape = GradientDrawable.RECTANGLE
                                        setColor(
                                                ContextCompat.getColor(
                                                        context,
                                                        TypeColorMapper.getColorForType(
                                                                pokemonType.type.name
                                                        )
                                                )
                                        )
                                    }
                            background = backgroundDrawable

                            val paddingHorizontal = (8 * resources.displayMetrics.density).toInt()
                            val paddingVertical = (4 * resources.displayMetrics.density).toInt()
                            setPadding(
                                    paddingHorizontal,
                                    paddingVertical,
                                    paddingHorizontal,
                                    paddingVertical
                            )
                            val margin = (4 * resources.displayMetrics.density).toInt()
                            layoutParams =
                                    ViewGroup.MarginLayoutParams(
                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                            .apply { rightMargin = margin }
                        }
                binding.typesContainer.addView(typeTextView)
            }

            Glide.with(itemView.context)
                    .load(pokemon.sprites.frontDefault)
                    .placeholder(R.drawable.ic_pokeball) // Show pokeball while loading
                    .error(R.drawable.ic_pokeball) // Show pokeball if loading fails
                    .into(binding.pokemonImage)

            // Capture Cost
            val cost = (pokemon.baseExperience / 20).coerceAtLeast(1)
            binding.captureCost.text = "Costo: $cost PB"

            binding.captureButton.setOnClickListener { onCaptureClick(pokemon) }
        }
    }
}

private class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}
