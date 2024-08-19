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
import androidx.compose.ui.unit.dp
import my.com.jobstreet.gradprogram.ui.theme.Gradprogram2024Theme

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bundle = receiveData()
        setContent {
            Gradprogram2024Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        bundle = bundle,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        openThirdActivity(bundle = bundle)
                    }
                }
            }
        }
    }

    private fun openThirdActivity(bundle: Bundle?) {
        val intent = Intent(this, ThirdActivity::class.java)
        bundle?.putBoolean("boolean", true)
        intent.putExtra("bundle1", bundle)
        startActivity(intent)
    }

    private fun receiveData() = intent.getBundleExtra("bundle")
}

@Composable
private fun Greeting(bundle: Bundle?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val text = if (bundle == null) {
        "Bundle is null"
    } else {
        "string=${bundle.getString("string")}\nint=${bundle.getInt("int")}"
    }
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = onClick) {
            Text(text = "Third Activity", style = MaterialTheme.typography.labelLarge)
        }
    }
}