package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.ui.detail.user.fragments.collection.CollectionFragment
import com.beva.bornmeme.ui.detail.user.fragments.comments.CommentsFragment
import com.beva.bornmeme.ui.detail.user.fragments.favorite.FavoriteFragment
import com.beva.bornmeme.ui.detail.user.fragments.posts.PostsFragment

class TabViewPagerAdapter(fragment: Fragment, val userId: String): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> FavoriteFragment.newInstance(userId)
            1 -> PostsFragment.newInstance(userId)
            2 -> CommentsFragment.newInstance(userId)
            3 -> CollectionFragment.newInstance(userId)
            else -> throw IllegalArgumentException("Don't Know")
        }
    }
}