package com.example.pokegame.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokegame.api.Pokemon
import com.example.pokegame.databinding.ItemCaptureBinding

// Reusing ItemCaptureBinding but changing button text programmatically or creating new layout?
// Let's reuse item_capture.xml but we'll change the button text to "LUCHAR" in onBind
class BattleSelectionAdapter(private val onBattleClick: (Pokemon) -> Unit) :
        ListAdapter<Pokemon, BattleSelectionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
            private val binding: ItemCaptureBinding,
            val onBattleClick: (Pokemon) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pokemon: Pokemon) {
            binding.pokemonName.text = pokemon.name.uppercase()

            Glide.with(binding.root).load(pokemon.sprites?.frontDefault).into(binding.pokemonImage)

            // Reuse types logic if possible, or just hide/show simple text
            binding.typesContainer.removeAllViews()
            // (Skipping complex type view creation for brevity, or we can copy from CaptureAdapter)

            binding.captureButton.text = "LUCHAR"
            binding.captureButton.setOnClickListener { onBattleClick(pokemon) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCaptureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onBattleClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}
