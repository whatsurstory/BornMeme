package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.beva.bornmeme.model.User
import com.beva.bornmeme.ui.detail.user.fragments.collection.CollectionFragment
import com.beva.bornmeme.ui.detail.user.fragments.comments.CommentsFragment
import com.beva.bornmeme.ui.detail.user.fragments.favorite.FavoriteFragment
import com.beva.bornmeme.ui.detail.user.fragments.posts.PostsFragment

class TabViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> FavoriteFragment()
            1 -> PostsFragment()
            2 -> CommentsFragment()
            3 -> CollectionFragment()
            else -> throw IllegalArgumentException("Don't Know")

        }
    }
}