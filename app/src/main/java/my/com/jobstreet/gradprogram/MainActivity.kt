package my.com.jobstreet.gradprogram

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        openSecondActivity()
                    }
                }
            }
        }
    }

    private fun openSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        val bundle = Bundle()
        bundle.putString("string", "Main Activity Value")
        bundle.putInt("int", 11)
        intent.putExtra("bundle", bundle)
        startActivity(intent)
    }
}

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Hello $name!",
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = onClick) {
            Text(text = "Second Activity", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Gradprogram2024Theme {
        Greeting(name = "Android", onClick = { })
    }
}