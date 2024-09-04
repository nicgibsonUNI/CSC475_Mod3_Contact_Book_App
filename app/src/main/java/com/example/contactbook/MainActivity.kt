package com.example.contactbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contacts = loadContacts() // Load saved contacts here

        setContent {
            ContactBookTheme {
                ContactBookApp(contacts, ::saveContacts)
            }
        }
    }

    // Function to save contacts to SharedPreferences
    private fun saveContacts(contacts: List<String>) {
        val sharedPreferences = getSharedPreferences("ContactBook", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("contacts", contacts.toSet())
        editor.apply()
    }

    // Function to load contacts from SharedPreferences
    private fun loadContacts(): MutableList<String> {
        val sharedPreferences = getSharedPreferences("ContactBook", MODE_PRIVATE)
        val contactSet =
            sharedPreferences.getStringSet("contacts", mutableSetOf()) ?: mutableSetOf()
        return contactSet.toMutableList()

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactBookApp(contacts: MutableList<String>, saveContacts: (List<String>) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var phone by remember { mutableStateOf(TextFieldValue()) }
    var selectedContactIndex by remember { mutableIntStateOf(-1) }
    val contactList = remember { mutableStateListOf(*contacts.toTypedArray()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Contact Book") }
            )
        },
        content = { paddingValues -> // Handle content inside the Scaffold's padding
            Column(
                modifier = Modifier
                    .padding(paddingValues) // Apply the padding from Scaffold
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input fields and Add Contact button
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Enter Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Enter Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Button(
                    onClick = {
                        if (name.text.isNotEmpty() && phone.text.isNotEmpty()) {
                            if (selectedContactIndex >= 0) {
                                // Update existing contact
                                contactList[selectedContactIndex] = "${name.text} - ${phone.text}"
                                selectedContactIndex = -1 // Reset index after updating
                            } else {
                                // Add new contact
                                contactList.add("${name.text} - ${phone.text}")
                            }
                            saveContacts(contactList) // Save contacts after adding or updating
                            name = TextFieldValue() // Reset fields
                            phone = TextFieldValue()
                        }
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
                ) {
                    Text(if (selectedContactIndex >= 0) "Update Contact" else "Add Contact", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable contact list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    contactList.forEachIndexed { index, contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Use different variable names for the split values to avoid shadowing
                            val (contactName, contactPhone) = contact.split(" - ")

                            // Stack the name and phone number vertically inside a column
                            Column(
                                modifier = Modifier.weight(1f) // Modifier.weight used to take available space
                            ) {
                                Text(text = contactName)
                                Text(text = contactPhone)
                            }

                            Row {
                                Button(onClick = {
                                    name = TextFieldValue(contactName) // Assign to TextFieldValue
                                    phone = TextFieldValue(contactPhone) // Assign to TextFieldValue
                                    selectedContactIndex = index
                                }) {
                                    Text("Edit")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    if (index < contactList.size) {
                                        contactList.removeAt(index)
                                        saveContacts(contactList) // Save after deletion
                                    }
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}



@Composable
fun ContactBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // Customize theme here
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun ContactBookPreview() {
    ContactBookTheme {
        ContactBookApp(
            contacts = mutableListOf("John Doe - 1234567890"),
            saveContacts = {}
        )
    }
}