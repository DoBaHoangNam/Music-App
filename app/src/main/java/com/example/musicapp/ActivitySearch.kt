package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.ActivitySearchBinding
import com.example.musicapp.model.Song
import com.example.musicapp.ui.MainActivity

class ActivitySearch : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SongAdapter
    private lateinit var originalMenuFood: MutableList<Song>
    private val filterMenuFood = mutableListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recvSearchList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(filterMenuFood)
        binding.recvSearchList.adapter = adapter
        setupSearchView()
        showAllMenu()

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupSearchView() {
        binding.searchEdt.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterMenuItems(query)
                return true
            }
        })
    }




    private fun filterMenuItems(query: String) {
        filterMenuFood.clear()

        originalMenuFood.forEach { item ->
            if (item.songName?.contains(
                    query,
                    ignoreCase = true
                ) == true || item.singerName?.contains(query, ignoreCase = true) == true
            ) {
                filterMenuFood.add(item)
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun showAllMenu() {
//        database = FirebaseDatabase.getInstance()
//        val foodRef: DatabaseReference = database.reference.child("menu")
        originalMenuFood = getListSong()

        filterMenuFood.clear()
        filterMenuFood.addAll(originalMenuFood)
        adapter.notifyDataSetChanged()

//        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for(foodSnapshot in snapshot.children){
//                    val menuItem  = foodSnapshot.getValue(Item::class.java)
//                    menuItem?.let { originalMenuFood.add(it) }
//                }
//                filterMenuFood.clear()
//                filterMenuFood.addAll(originalMenuFood)
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("DatabaseError", "Error: ${error.message}")
//            }
//        })


    }


    private fun getListSong(): MutableList<Song> {
        val list = mutableListOf<Song>()
        list.add(Song("Tây Bắc Thả Chiều Vào Tranh ", "Sèn Hoàng Mỹ Lam", R.drawable.img_song))
        list.add(
            Song(
                "Tây Bắc Yêu thương ",
                "Sèn Hoàng Mỹ Lam - Nguyễn Thu Hằng",
                R.drawable.img_song
            )
        )
        list.add(Song("Chắc ai đó sẽ về", "Sơn Trung MPT", R.drawable.img_song))
        list.add(Song("Panis Angelicus", "franck", R.drawable.img_song))
        list.add(Song("Có Ai Thương Em Như Anh", "Tóc Tiên", R.drawable.img_song))
        list.add(Song("Tàu Anh Qua Nui", "Anh Thơ", R.drawable.img_song))
        return list

    }
}