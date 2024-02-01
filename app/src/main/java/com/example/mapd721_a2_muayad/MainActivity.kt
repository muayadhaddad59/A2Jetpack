package com.example.mapd721aonemuayad

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mapd721_a2_muayad.ui.theme.MAPD721A2MuayadTheme
import kotlinx.coroutines.launch
import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri

class MainActivity : ComponentActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            MAPD721A2MuayadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactManager(context = applicationContext)
                }
            }
        }
        requestContactsPermission()
    }

    private fun requestContactsPermission() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the app's functionality
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}

@Composable
fun ContactManager(context: Context) {
    var contactName by remember { mutableStateOf("") }
    var contactNO by remember { mutableStateOf("") }
    var contactsList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Contact Name") }
        )
        Spacer(modifier = Modifier.padding(top = 12.dp))
        OutlinedTextField(
            value = contactNO,
            onValueChange = { contactNO = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Contact No") }
        )

        Spacer(modifier = Modifier.padding(top = 20.dp))

        ButtonRow(
            onFetchContacts = {
                coroutineScope.launch {
                    contactsList = fetchContacts(context)
                }
            },
            onAddContact = {
                    addContact(context = context, name = contactName, number = contactNO)

                }
        )

        Spacer(modifier = Modifier.padding(top = 20.dp))

        LazyColumn {
            items(contactsList) { contact ->
                ContactItem(name = contact.first, number = contact.second)
            }
        }


    }
}

fun addContact(context: Context, name: String, number: String): Boolean {
    val values = ContentValues().apply {
        put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
        put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
    }

    val rawContactUri: Uri? = context.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values)
    rawContactUri?.let { rawContactUri ->
        val rawContactId = ContentUris.parseId(rawContactUri)
        val contactValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contactValues)

        val phoneValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
        return true
    }
    return false
}


@SuppressLint("Range")
fun fetchContacts(context: Context): List<Pair<String, String>> {
    val contacts = mutableListOf<Pair<String, String>>()
    val cursor: Cursor? = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contacts.add(name to number)
        }
    }

    cursor?.close()

    return contacts
}




@Composable
fun AboutSection(studentName: String, studentID: String) {
    Column {
        Text("About", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Student Name: $studentName", style = MaterialTheme.typography.bodyLarge)
        Text("Student ID: $studentID", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ContactItem(name: String, number: String) {
    Column {
        Text(text = "Name: $name", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Number: $number", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ButtonRow(
    onFetchContacts: () -> Unit,
    onAddContact: () -> Unit
) {
    Row {
        Button(onClick = { onFetchContacts() }) {
            Text(text = "Fetch Contacts")
        }
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Button(onClick = { onAddContact() }) {
            Text(text = "Add Contact")
        }
    }
}

