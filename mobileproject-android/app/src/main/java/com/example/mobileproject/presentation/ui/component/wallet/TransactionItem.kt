package com.example.mobileproject.presentation.ui.component.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionType
import com.example.mobileproject.domain.model.ExpenseCategory
import com.example.mobileproject.utils.formatSimpleAmount
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionItem(transaction: Transaction, goals: List<SavingGoal> = emptyList()) {
    var showDetail by remember { mutableStateOf(false) }
    val isExpense = transaction.type == TransactionType.EXPENSE
    
    // Tìm goal liên quan dựa trên Note (thường backend trả về note có chứa goal name hoặc goalId)
    // Hoặc so khớp category nếu note có từ khóa đặc biệt
    val relatedGoal = remember(transaction, goals) {
        goals.find { 
            transaction.note.contains(it.name, ignoreCase = true) || 
            transaction.note.contains(it.id, ignoreCase = true)
        }
    }

    val displayTitle = relatedGoal?.name ?: transaction.category.replace("_", " ").uppercase()
    
    // Ánh xạ icon từ Category ID (nếu là goal thì lấy category của goal đó)
    val categoryId = relatedGoal?.category ?: transaction.category
    val categoryInfo = remember(categoryId) {
        ExpenseCategory.getAll().find { it.id == categoryId } ?: ExpenseCategory.Others
    }

    val dateDisplay = remember(transaction.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(transaction.createdAt)
            date?.let { outputFormat.format(it) } ?: transaction.createdAt
        } catch (e: Exception) {
            transaction.createdAt.split("T").first()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetail = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFF0F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryInfo.icon,
                        contentDescription = null,
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Text(
                text = "${if (isExpense) "-" else "+"}${formatSimpleAmount(transaction.amount)}",
                color = if (isExpense) Color(0xFFE57373) else Color(0xFF81C784),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    if (showDetail) {
        TransactionDetailDialog(
            transaction = transaction,
            displayTitle = displayTitle,
            formattedDate = dateDisplay,
            categoryInfo = categoryInfo,
            onDismiss = { showDetail = false }
        )
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    displayTitle: String,
    formattedDate: String,
    categoryInfo: ExpenseCategory,
    onDismiss: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    
    val timeDisplay = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(transaction.createdAt)
        date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) { "" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Transaction Detail", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFFF0F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryInfo.icon, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${if (isExpense) "-" else "+"}${formatSimpleAmount(transaction.amount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isExpense) Color(0xFFE57373) else Color(0xFF81C784)
                )
                
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFFFF0F0))
                Spacer(modifier = Modifier.height(24.dp))

                DetailRow("Activity", displayTitle)
                DetailRow("Note", transaction.note.ifBlank { "No content" })
                DetailRow("Date", formattedDate)
                DetailRow("Full Time", timeDisplay)
                DetailRow("Owner ID", transaction.coupleId.split(":").last()) 

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            color = Color(0xFF2D2D2D), 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}
