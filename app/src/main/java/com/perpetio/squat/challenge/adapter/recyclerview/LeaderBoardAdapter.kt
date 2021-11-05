package com.perpetio.squat.challenge.adapter.recyclerview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.databinding.ItemLeaderBoardBinding
import com.perpetio.squat.challenge.model.domain.PlayerModel

class LeaderBoardAdapter :
    ListAdapter<PlayerModel, LeaderBoardAdapter.LeaderBoardViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardViewHolder {
        val binding =
            ItemLeaderBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class LeaderBoardViewHolder(val binding: ItemLeaderBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(player: PlayerModel, position: Int) {
            binding.name.text = player.name
            binding.score.text = player.score
            binding.position.text = position.toString()
            setPositionImageAndTextColor(position)
        }

        private fun setPositionImageAndTextColor(position: Int) {
            when (position) {
                1 -> {
                    binding.placeBgIv.visibility = View.VISIBLE
                    binding.position.setTextColor(Color.WHITE)
                    binding.placeBgIv.setImageResource(R.drawable.ic_bg_first_place)
                }
                2 -> {
                    binding.placeBgIv.visibility = View.VISIBLE
                    binding.position.setTextColor(Color.WHITE)
                    binding.placeBgIv.setImageResource(R.drawable.ic_bg_second_place)
                }
                3 -> {
                    binding.placeBgIv.visibility = View.VISIBLE
                    binding.position.setTextColor(Color.WHITE)
                    binding.placeBgIv.setImageResource(R.drawable.ic_bg_third_place)
                }
                else -> {
                    binding.position.setTextColor(
                        ContextCompat.getColor(
                            binding.name.context,
                            R.color.purple_main
                        )
                    )
                    binding.placeBgIv.visibility = View.GONE
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlayerModel>() {

        override fun areItemsTheSame(oldItem: PlayerModel, newItem: PlayerModel): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: PlayerModel, newItem: PlayerModel): Boolean =
            oldItem == newItem
    }
}