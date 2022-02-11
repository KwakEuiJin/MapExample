package com.example.part4_chapter3

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.part4_chapter3.MapActivity.Companion.SEARCH_RESULT_EXTRA_KEY
import com.example.part4_chapter3.databinding.ActivityMainBinding
import com.example.part4_chapter3.model.LocationLatLngEntity
import com.example.part4_chapter3.model.SearchResultEntity
import com.example.part4_chapter3.response.search.Pois
import com.example.part4_chapter3.utillity.RetrofitUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class MainActivity() : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    private lateinit var binding: ActivityMainBinding
    private val searchRecyclerViewAdapter: SearchRecyclerViewAdapter by lazy {
        SearchRecyclerViewAdapter(searchResultClickListener = {
            Log.d("클릭리스너", "${it.fullAdress} ${it.name} ${it.locationLatLng}")
            val intent = Intent(this,MapActivity::class.java)
            intent.putExtra(SEARCH_RESULT_EXTRA_KEY,it)
            startActivity(intent)

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()

        initViews()
        bindViews()
        initAdapter()
        initData()

    }


    private fun initAdapter() {

        binding.recyclerView.apply {
            adapter = searchRecyclerViewAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun initViews() = with(binding) {
        nonResearchTextView.isVisible = false

    }

    private fun bindViews() = with(binding) {
        searchButton.setOnClickListener {
            searchKeyword(locationInputEditText.text.toString())

        }
    }


    private fun initData() {


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(pois: Pois) {
        val dataList = pois.poi.map {
            SearchResultEntity(
                fullAdress = it.upperAddrName.toString(),
                name = it.name ?:"빌딩명 없음",
                locationLatLng = LocationLatLngEntity(
                    it.noorLat, it.noorLon
                )
            )
        }
        Log.d("리스트", dataList.toString())
        searchRecyclerViewAdapter.submitList(dataList)
        searchRecyclerViewAdapter.notifyDataSetChanged()

    }

    private fun searchKeyword(keyword:String) {
        launch(coroutineContext) {
            try {
                with(Dispatchers.IO){
                    val response = RetrofitUtil.apiService.getSearchLocation(
                        keyword = keyword
                    )
                    if (response.isSuccessful){
                        val body = response.body()
                        withContext(Dispatchers.Main){
                            Log.e("response",body.toString())
                            body?.let {searchResponse ->
                                setData(searchResponse.searchPoiInfo.pois)
                            }
                        }
                    }
                }

            }catch (e:Exception){

            }
        }

    }

}