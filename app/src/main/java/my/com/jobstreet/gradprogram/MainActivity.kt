package my.com.jobstreet.gradprogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import my.com.jobstreet.gradprogram.ui.theme.Gradprogram2024Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Gradprogram2024Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Group(paddingValues = innerPadding)
                }
            }
        }
    }
}

@Composable
private fun Group(paddingValues: PaddingValues = PaddingValues()) {
    Column(
        modifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        GroupTitle()
        GroupDescription()
    }
}

@Composable
private fun GroupTitle() {
    Row {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = "Local Group 1", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(height = 8.dp))
            Text(text = "1k followers", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.width(width = 8.dp))
        Button(onClick = { }) {
            Text(text = "Follow", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun GroupDescription() {
    Spacer(modifier = Modifier.height(height = 16.dp))
    Text(
        text = "Description for local group to help candidates understand how this could help. MAX 2 lines.",
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewGroup() {
    Gradprogram2024Theme {
        Group()
    }
}
