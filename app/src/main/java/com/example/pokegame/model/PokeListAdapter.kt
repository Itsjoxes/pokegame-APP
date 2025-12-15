package com.example.pokegame.model

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokegame.R
import com.example.pokegame.api.Pokemon
import com.example.pokegame.databinding.ItemLoadingIndicatorBinding
import com.example.pokegame.databinding.PokeListBinding
import com.example.pokegame.model.UiModel.PokemonItem
import com.example.pokegame.util.RarityBorderMapper
import com.example.pokegame.util.TypeColorMapper

private const val VIEW_TYPE_POKEMON = 0
private const val VIEW_TYPE_LOADING = 1

sealed class UiModel {
    data class PokemonItem(val pokemon: Pokemon) : UiModel()
    object LoadingItem : UiModel()
}

class PokeListAdapter(private val pokemonClick: (Int) -> Unit) :
        ListAdapter<UiModel, RecyclerView.ViewHolder>(UiModelDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PokemonItem -> VIEW_TYPE_POKEMON
            UiModel.LoadingItem -> VIEW_TYPE_LOADING
            null -> throw IllegalStateException("null item found")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_POKEMON) {
            val binding =
                    PokeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PokemonViewHolder(binding)
        } else {
            val binding =
                    ItemLoadingIndicatorBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    )
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PokemonViewHolder) {
            val pokemonItem = getItem(position) as PokemonItem
            holder.bind(pokemonItem.pokemon, pokemonClick)
        }
    }

    class PokemonViewHolder(private val binding: PokeListBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(pokemon: Pokemon, pokemonClick: (Int) -> Unit) {
            binding.pokemonName.text = (pokemon.name ?: "Desconocido").uppercase()
            binding.pokemonName.setTypeface(null, Typeface.BOLD)

            // Set rarity border
            val borderColor = RarityBorderMapper.getBorderColorForRarity(pokemon)
            binding.card.strokeColor = ContextCompat.getColor(itemView.context, borderColor)

            binding.typesContainer.removeAllViews()
            pokemon.types?.forEach { pokemonType ->
                val typeTextView =
                        TextView(itemView.context).apply {
                            text = pokemonType.type.name.take(3).uppercase()

                            val typeface = ResourcesCompat.getFont(context, R.font.jersey_10)
                            setTypeface(typeface, Typeface.BOLD)

                            setTextColor(Color.WHITE)
                            letterSpacing = 0.1f // Add letter spacing

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
                    .load(pokemon.sprites?.frontDefault)
                    .placeholder(R.drawable.ic_pokeball) // Show pokeball while loading
                    .error(R.drawable.ic_pokeball) // Show pokeball if loading fails
                    .into(binding.pokemonImage)

            itemView.setOnClickListener { pokemonClick(pokemon.id) }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingIndicatorBinding) :
            RecyclerView.ViewHolder(binding.root)
}
