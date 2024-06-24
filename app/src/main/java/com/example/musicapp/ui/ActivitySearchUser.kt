package com.example.musicapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.OnRecentSearchItemClickListener
import com.example.musicapp.RecentSearchListener
import com.example.musicapp.adapter.RecentSearchAdapter
import com.example.musicapp.adapter.UserSearchAdapter
import com.example.musicapp.databinding.ActivitySearchBinding
import com.example.musicapp.model.RecentSearch
import com.example.musicapp.model.Song
import com.example.musicapp.model.User
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class ActivitySearchUser : AppCompatActivity(), OnRecentSearchItemClickListener {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: UserSearchAdapter
    private lateinit var adapter2: RecentSearchAdapter
    private val filterUser = mutableListOf<User>()
    private var listOfUser = mutableListOf<User>()
    private val recentSearch = mutableListOf<RecentSearch>()
    private val PREF_NAME = "MyPrefs"
    private val KEY_RECENT_USER_SEARCH = "recent_user_search"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        recentSearch.addAll(getRecentSearchFromSharedPreferences())
        databaseReference = FirebaseDatabase.getInstance().reference

        val songToShare = intent.getParcelableExtra<Song>("song_name")
        if (songToShare != null) {
            Log.d("ActivitySearchUser1", "Received song: $songToShare")
            // Do something with songToShare
        } else {
            Log.d("ActivitySearchUser1", "No song received")
        }

        fetchUsers{users ->
            listOfUser = users as MutableList<User>
            Log.d("ActivitySearchUser1", listOfUser.toString() + " listOfUser")
        }


        binding.recvSearchList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = UserSearchAdapter(this, filterUser, songToShare as Song)
        binding.recvSearchList.adapter = adapter

        setupSearchView()
        showRecent()


        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getRecentSearchFromSharedPreferences(): MutableList<RecentSearch> {
        val json = sharedPreferences.getString(KEY_RECENT_USER_SEARCH, null)
        val type = object : TypeToken<MutableList<RecentSearch>>() {}.type
        return Gson().fromJson(json, type) ?: mutableListOf()
    }

    private fun saveRecentSearchToSharedPreferences(recentSearchList: MutableList<RecentSearch>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(recentSearchList)
        editor.putString(KEY_RECENT_USER_SEARCH, json)
        editor.apply()
    }

    private fun showRecent() {
        binding.headingOfRecTV.text = "Recent Searches"
        binding.recvRecentList.visibility = View.VISIBLE
        binding.recvSearchList.visibility = View.INVISIBLE

        binding.recvRecentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter2 = RecentSearchAdapter(recentSearch, this, object : RecentSearchListener {
            override fun saveRecentSearch(recentSearchList: MutableList<RecentSearch>) {
                saveRecentSearchToSharedPreferences(recentSearchList)
            }
        })
        binding.recvRecentList.adapter = adapter2

    }

    private fun addToRecentSearchIfNotExist(query: String) {
        if (!recentSearch.any { it.content == query }) {
            recentSearch.add(RecentSearch(query))
            adapter2.notifyDataSetChanged()
            saveRecentSearchToSharedPreferences(recentSearch)
        }
    }


    private fun setupSearchView() {
        binding.searchEdt.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterSongItems(query)
                addToRecentSearchIfNotExist(query.trim())
                adapter2.notifyDataSetChanged()
                saveRecentSearchToSharedPreferences(recentSearch)
                if (query.isEmpty()) {
                    showRecent()
                }
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterSongItems(query)
                binding.headingOfRecTV.text = "User"

                if (query.isEmpty()) {
                    showRecent()
                }
                return true
            }
        })
    }


    private fun filterSongItems(query: String) {
        binding.recvRecentList.visibility = View.INVISIBLE
        binding.recvSearchList.visibility = View.VISIBLE

        filterUser.clear()

        listOfUser.forEach { item ->
            if (item.username?.contains(
                    query,
                    ignoreCase = true
                ) == true || item.email?.contains(query, ignoreCase = true) == true
            ) {
                filterUser.add(item)
            }
        }

        Log.d("ActivitySearchUser1", "$filterUser filterlistOfUser")

        adapter.notifyDataSetChanged()
    }



    private fun fetchUsers(callback: (List<User>) -> Unit) {
        databaseReference.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        user.id = userSnapshot.key // Assign the key as the user id
                        userList.add(it)
                    }
                }
                callback(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.e("ActivitySearchUser1", "Failed to fetch users", databaseError.toException())
            }
        })
    }



    override fun onItemClick(searchContent: String) {
        binding.searchEdt.setQuery(searchContent, true)
    }
}