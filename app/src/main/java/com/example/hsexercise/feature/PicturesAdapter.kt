package com.example.hsexercise.feature

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hsexercise.R
import com.example.hsexercise.databinding.PictureItemBinding
import com.example.hsexercise.feature.database.FeatureModel

class PicturesAdapter(
    private val news: List<FeatureModel>
) : RecyclerView.Adapter<PicturesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewBinding = PictureItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return news.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(news[position])
    }

    class ViewHolder(
        private val viewBinding: PictureItemBinding
    ) : RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(picture: FeatureModel) {
            viewBinding.apply {
                author.text = picture.author
                desc.text = root.context.getString(R.string.image_dimensions, picture.width, picture.height)

                with(picture) {
                    // To download images with original size, replace IMAGE_WIDTH and IMAGE_HEIGHT with width and height
                    val url = "$PICTURE_BASE_URL/$id/$IMAGE_WIDTH/$IMAGE_HEIGHT"
                    picture.url = url

                    val options = RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.picture_image_placeholder)
                        .error(R.drawable.picture_image_placeholder)
                    Glide.with(image.context)
                        .load(url)
                        .apply(options)
                        .into(image)
                }
            }
        }
    }

    private companion object {
        private const val PICTURE_BASE_URL = "https://picsum.photos/id"
        private const val IMAGE_WIDTH = 300
        private const val IMAGE_HEIGHT = 194
    }
}