package my.com.jobstreet.gradprogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import my.com.jobstreet.gradprogram.ui.theme.Gradprogram2024Theme

class ThirdActivity : ComponentActivity() {
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
                    )
                }
            }
        }
    }

    private fun receiveData() = intent.getBundleExtra("bundle1")
}

@Composable
private fun Greeting(bundle: Bundle?, modifier: Modifier = Modifier) {
    val text = if (bundle == null) {
        "Bundle is null"
    } else {
        "string=${bundle.getString("string")}\n" +
                "int=${bundle.getInt("int")}\n" +
                "boolean=${bundle.getBoolean("boolean")}"
    }
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}