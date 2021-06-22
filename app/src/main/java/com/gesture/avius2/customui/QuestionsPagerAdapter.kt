package com.gesture.avius2.customui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class QuestionsPagerAdapter(
    private val lifecycle: Lifecycle,
    fm: FragmentManager,
    private var fragments: List<Fragment> = listOf()
) : FragmentStateAdapter(fm, lifecycle) {

    fun setList(fms: List<Fragment>) {
        fragments = fms
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

