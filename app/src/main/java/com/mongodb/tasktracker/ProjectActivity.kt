package com.mongodb.tasktracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongodb.tasktracker.model.Project
import com.mongodb.tasktracker.model.ProjectAdapter
import com.mongodb.tasktracker.model.User
import io.realm.OrderedRealmCollection
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.mongodb.sync.SyncConfiguration

/*
* ProjectActivity: allows a user to view a collection of Projects. Clicking on a project launches a
* view of tasks in that project. Clicking on the options button for a project launches a view
* that allows the user to add or remove members from the project. All projects are stored in a
* read-only realm on the logged in user's User object.
*/
class ProjectActivity : AppCompatActivity() {
    private var user: io.realm.mongodb.User? = null
    private var userRealm: Realm? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectAdapter

    override fun onStart() {
        super.onStart()
        user = taskApp.currentUser()
        if (user == null) {
            // if no user is currently logged in, start the login activity so the user can authenticate
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // TODO: initialize a connection to a realm containing the user's User object
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        recyclerView = findViewById(R.id.project_list)
    }

    // TODO: always ensure that the user realm closes when the activity ends via the onStop lifecycle method

    // TODO: always ensure that the user realm closes when the activity ends via the onDestroy lifecycle method

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_task_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                user?.logOutAsync {
                    if (it.isSuccess) {
                        user = null
                        Log.v(TAG(), "user logged out")
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Log.e(TAG(), "log out failed! Error: ${it.error}")
                    }
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setUpRecyclerView(realm: Realm) {
        // query for a user object in our user realm, which should only contain our user object
        // TODO: query the realm to get a copy of the currently logged in user's User object (or null, if the trigger didn't create it yet)
        var syncedUser : User? = null

        // if a user object exists, create the recycler view and the corresponding adapter
        if (syncedUser != null) {
            val projectsList = syncedUser.memberOf
            adapter = ProjectAdapter(projectsList, user!!)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
            )
        } else {
            // since a trigger creates our user object after initial signup, the object might not exist immediately upon first login.
            // if the user object doesn't yet exist (that is, if there are no users in the user realm), call this function again when it is created
            Log.i(TAG(), "User object not yet initialized, waiting for initialization via Trigger before displaying projects.")
            // change listener on a query for our user object lets us know when the user object has been created by the auth trigger
            // TODO: set up a change listener that will set up the recycler view once our trigger initializes the user's User object
        }
    }
}
