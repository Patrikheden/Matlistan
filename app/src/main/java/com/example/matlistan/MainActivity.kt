
package com.example.matlistan

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import com.example.matlistan.ui.theme.MatlistanTheme

data class FoodItem(
    val name: String,
    val checked: Boolean = false
)

private val Green = Color(0xFF477A59)
private val LightGreen = Color(0xFFE7F0E8)
private val Background = Color(0xFFF6F8F5)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MatlistanTheme {
                ShoppingScreen(this)
            }
        }
    }
}

@Composable
fun ShoppingScreen(context: Context) {

    var item by remember { mutableStateOf("") }

    val shoppingList = remember {
        mutableStateListOf<FoodItem>().apply {

            val saved = context.getSharedPreferences(
                "matlista",
                Context.MODE_PRIVATE
            ).getString("items", "[]")

            try {
                val json = JSONArray(saved)

                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)

                    add(
                        FoodItem(
                            name = obj.getString("name"),
                            checked = obj.getBoolean("checked")
                        )
                    )
                }
            } catch (_: Exception) {
                clear()
            }
        }
    }

    fun saveList() {

        val json = JSONArray()

        shoppingList.forEach { food ->

            json.put(
                JSONObject().apply {
                    put("name", food.name)
                    put("checked", food.checked)
                }
            )
        }

        context.getSharedPreferences(
            "matlista",
            Context.MODE_PRIVATE
        ).edit()
            .putString("items", json.toString())
            .apply()
    }

    val bought = shoppingList.count { it.checked }

    Scaffold(
        containerColor = Background,

        topBar = {
            Surface(color = Green) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Matlistan 🌿",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Text(
                        text = "Din smarta inköpslista",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                text = "Lägg till en vara",
                style = MaterialTheme.typography.titleMedium,
                color = Green
            )

            Spacer(modifier = Modifier.height(10.dp))

            // TEXTFÄLT MED STÖD FÖR Å Ä Ö

            BasicTextField(
                value = item,

                onValueChange = { newValue ->
                    item = newValue
                },

                singleLine = true,

                textStyle = TextStyle(
                    color = Color.DarkGray,
                    fontSize = 16.sp
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp),

                decorationBox = { innerTextField ->

                    Box {

                        if (item.isEmpty()) {
                            Text(
                                text = "Ex. Mjölk, bröd, ägg...",
                                color = Color.Gray
                            )
                        }

                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    val name = item.trim()

                    if (name.isNotEmpty()) {

                        shoppingList.add(
                            FoodItem(name)
                        )

                        item = ""

                        saveList()
                    }
                },

                enabled = item.isNotBlank(),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Green
                ),

                shape = RoundedCornerShape(14.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {

                Text(
                    text = "+  Lägg till",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Min inköpslista",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green
                )

                Text(
                    text = "$bought av ${shoppingList.size} köpta",
                    color = Green
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (shoppingList.isNotEmpty()) {

                LinearProgressIndicator(
                    progress = {
                        bought.toFloat() /
                                shoppingList.size.coerceAtLeast(1)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),

                    color = Green,
                    trackColor = LightGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shoppingList.isEmpty()) {

                Text(
                    text = "Din inköpslista är tom 🌿",
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    textAlign = TextAlign.Center
                )

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    itemsIndexed(shoppingList) { index, food ->

                        Card(
                            shape = RoundedCornerShape(16.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(
                                    checked = food.checked,

                                    onCheckedChange = { checked ->

                                        shoppingList[index] =
                                            food.copy(checked = checked)

                                        saveList()
                                    },

                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Green
                                    )
                                )

                                Text(
                                    text = food.name,

                                    modifier = Modifier.weight(1f),

                                    style = MaterialTheme.typography.bodyLarge,

                                    color = if (food.checked)
                                        Color.Gray
                                    else
                                        Color.DarkGray
                                )

                                TextButton(
                                    onClick = {

                                        shoppingList.removeAt(index)

                                        saveList()
                                    }
                                ) {

                                    Text(
                                        text = "Ta bort",
                                        color = Color(0xFFB65B52)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}