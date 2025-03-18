package com.example.filmcataloge.uiConfiguration.reviewsAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filmcataloge.R
import com.example.filmcataloge.databinding.ReviewItemBinding
import com.example.filmcataloge.netConfiguration.reviewDetails.Result
import com.example.filmcataloge.uiConfiguration.moviesAdapter.BASE_URL_FOR_IMAGES

class ReviewsAdapter() : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    private var reviews: List<Result> = ArrayList()

    class ReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ReviewItemBinding = ReviewItemBinding.bind(itemView)

        fun bind(result: Result) = with(binding) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(150, 200)
                .placeholder(R.drawable.loading)
                .error(R.drawable.blank_user)

            Glide.with(authorAvatar.context)
                .load(BASE_URL_FOR_IMAGES + result.author_details.avatar_path)
                .apply(requestOptions)
                .into(authorAvatar)

            authorName.text = result.author
            dateOfReviewUploaded.text = result.created_at
            reviewContent.text = result.content
            ratingOfFilm.text = result.author_details.rating.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val review = reviews[position]
        if (position >= 1){
            holder.itemView.setPadding(100, 0, 0, 0)
        }
        holder.bind(review)
    }

    fun setReviews(reviews: List<Result>) {
        this.reviews = reviews
        notifyItemRangeChanged(0, reviews.size)
    }
}