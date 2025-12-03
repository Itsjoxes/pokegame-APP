package com.example.pokegame.model

import androidx.recyclerview.widget.DiffUtil

class UiModelDiffCallback : DiffUtil.ItemCallback<UiModel>() {
    override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
        return (oldItem is UiModel.PokemonItem && newItem is UiModel.PokemonItem && oldItem.pokemon.id == newItem.pokemon.id) ||
               (oldItem is UiModel.LoadingItem && newItem is UiModel.LoadingItem)
    }

    override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
        return oldItem == newItem
    }
}