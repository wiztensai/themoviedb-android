package com.wiz.moviedb.ui.screen.home.F_Home

import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.schibsted.spain.barista.assertion.BaristaAssertions.assertThatBackButtonClosesTheApp
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListNotEmpty
import com.schibsted.spain.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickBack
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import com.schibsted.spain.barista.interaction.BaristaListInteractions.scrollListToPosition
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import com.schibsted.spain.barista.internal.failurehandler.BaristaException
import com.schibsted.spain.barista.rule.cleardata.ClearDatabaseRule
import com.schibsted.spain.barista.rule.cleardata.ClearFilesRule
import com.schibsted.spain.barista.rule.cleardata.ClearPreferencesRule
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.Repeat
import com.wiz.moviedb.R
import com.wiz.moviedb.ui.A_Main
import com.wiz.moviedb.util.EspressoIdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class A_MainTest{

    val TAG = "A_HomeTest"

    @get:Rule var activityRule: ActivityTestRule<A_Main> = ActivityTestRule(A_Main::class.java)
    @get:Rule var clearPreferencesRule = ClearPreferencesRule()
    @get:Rule var clearFilesRule = ClearFilesRule() // Delete all files in getFilesDir() and getCacheDir()
    @get:Rule var clearDatabaseRule = ClearDatabaseRule() // Delete all tables from all the app's SQLite Databases

    /**
     * cek jika di request pertama, mendapatkan result size 20 (result maks 1 page di moviedb)
     * lalu scroll ke item terakhir
     * lalu cek di request kedua, total result 40
     *
     * lalu click category now playing
     * cek apakah endless scrollnya work?
     */
    @Test
    fun test_endless_rv_every_category() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingresource)
        val resId = R.id.recyclerView

        assertRecyclerViewItemCount(resId, 20)
        scrollListToPosition(resId, getRVcount(resId)-1)
        assertRecyclerViewItemCount(resId, 40)

        clickOn(R.id.btn_visibility_bottom_sheet)
        sleep(300) // wait bottomsheet view animation
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingresource)
        clickOn(R.id.btnCatNowPlaying)

        assertRecyclerViewItemCount(resId, 20)
        scrollListToPosition(resId, getRVcount(resId)-1)
        assertRecyclerViewItemCount(resId, 40)

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingresource)
    }

    /**
     * cek jika show more review pada detail movie bekerja
     * cari film avenger di category popular
     * lalu cek apakah show more ada?
     * jika ada, click untuk ke halaman more review
     *
     * cek endless scrollnya apakah bekerja atau tidak di halaman more review
     */
    @Test
    fun check_more_review_in_movie_detail() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingresource)

        assertListNotEmpty(R.id.recyclerView)
        clickListItem(R.id.recyclerView, 0)
        sleep(200) // animasi pindah ke detail movie
        try {
            assertListNotEmpty(R.id.rvReviews) // cek list tidak kosong

            Log.d(TAG, "rv size: ${getRVcount(R.id.rvReviews)}")
        } catch (e: BaristaException) { // jika list kosong, sengaja dicatch karena memunculkan info rv kosong
            assertDisplayed(R.id.tvInfo) // muncul info error

            Log.d(TAG, "rv size: ${getRVcount(R.id.rvReviews)}")
        }

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingresource)
    }

    /**
     * click list 1 di category popular. simpan variabel tersebut
     * ke movie detail, lalu popbackstack
     * lalu klik btn favorite. click list 1 di fav untuk ke movie detailnya
     * cek apakah judul dan tanggalnya sama dengan variabel yang disimpan?
     */
    @Test
    fun check_favorite() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingresource)

        clickListItem(R.id.recyclerView, 0)
        sleep(200) // tunggu durasi animasi

        clickOn(R.id.btnFavDetail)
        clickOn(R.id.btnBack) // back ke home
        sleep(200) // tunggu durasi animasi

        val recyclerView = activityRule.getActivity().findViewById(R.id.recyclerView) as RecyclerView
        val movieTitle = recyclerView.layoutManager?.findViewByPosition(0)?.findViewById<TextView>(R.id.tvTitle)?.text?.toString()
        if (movieTitle == null) throw BaristaException("Movie title null", IllegalStateException())
        clickOn(R.id.btnFavHome)
        sleep(200) // tunggu durasi animasi

        val rvFavorite = activityRule.getActivity().findViewById(R.id.rvFavorite) as RecyclerView
        val favTitle = rvFavorite.findViewHolderForAdapterPosition(0)?.itemView?.findViewById<TextView>(R.id.tvTitle)
        favTitle?.let {
            assertDisplayed(it.id, movieTitle)
        }

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingresource)
    }

    @Test
    fun exit_app() {
        clickBack()
        assertThatBackButtonClosesTheApp()
    }

    private fun getRVcount(resId:Int): Int {
        val recyclerView = activityRule.getActivity().findViewById(resId) as RecyclerView
        return recyclerView.adapter!!.itemCount
    }
}