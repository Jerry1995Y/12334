package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPrefs
import com.example.model.ShoppingItem
import com.example.ui.theme.YirenjuTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors

    var shoppingItems by remember { mutableStateOf(prefs.getShopping()) }

    var newItemText by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("食材") }

    // 12 Common chips with default category
    val commonItems = remember {
        listOf(
            "鸡蛋" to "食材",
            "牛奶" to "食材",
            "面包" to "食材",
            "青菜" to "食材",
            "牛肉" to "食材",
            "大米" to "食材",
            "水果" to "食材",
            "洗衣液" to "日用品",
            "纸巾" to "日用品",
            "洗发水" to "日用品",
            "牙膏" to "日用品",
            "垃圾袋" to "日用品"
        )
    }

    val addItem = { text: String, group: String ->
        if (text.isNotBlank()) {
            val updated = shoppingItems.toMutableList()
            updated.add(ShoppingItem(System.currentTimeMillis(), text.trim(), group, false))
            shoppingItems = updated
            prefs.saveShopping(updated)
            Toast.makeText(context, "已加入清单：$text", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 常用（点一下加入清单）Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(text = "常用（点一下加入清单）", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonItems.forEach { (name, grp) ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surface2)
                                .border(1.dp, colors.border, CircleShape)
                                .clickable { addItem(name, grp) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = name, color = colors.text, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 采购清单 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(text = "采购清单", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Input + ＋ button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = { Text("加一项，如 鸡蛋", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface2,
                            unfocusedContainerColor = colors.surface2,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                addItem(newItemText, selectedGroup)
                                newItemText = ""
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable {
                                addItem(newItemText, selectedGroup)
                                newItemText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "＋", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Group toggle chips (食材, 日用品)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("食材", "日用品").forEach { grp ->
                        val isSelected = selectedGroup == grp
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) colors.accent else colors.surface2)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, CircleShape)
                                .clickable { selectedGroup = grp }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = grp,
                                color = if (isSelected) Color.White else colors.text,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shopping item list
                shoppingItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tick checkmark (44x48 min touch target)
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable {
                                    val updated = shoppingItems.toMutableList()
                                    updated[index] = item.copy(done = !item.done)
                                    shoppingItems = updated
                                    prefs.saveShopping(updated)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        if (item.done) colors.ok else colors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.done) {
                                    Text(text = "✓", color = colors.ok, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Text
                        Text(
                            text = item.text,
                            color = if (item.done) colors.muted else colors.text,
                            fontSize = 15.sp,
                            textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        // Tag
                        Text(
                            text = item.group,
                            color = colors.muted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        // Delete
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable {
                                    val updated = shoppingItems.toMutableList()
                                    updated.removeAt(index)
                                    shoppingItems = updated
                                    prefs.saveShopping(updated)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "×", color = colors.muted, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 清空已买 Button
                Button(
                    onClick = {
                        val updated = shoppingItems.filter { !it.done }
                        shoppingItems = updated
                        prefs.saveShopping(updated)
                        Toast.makeText(context, "已清除已买项目", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("清空已买", color = colors.text, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
