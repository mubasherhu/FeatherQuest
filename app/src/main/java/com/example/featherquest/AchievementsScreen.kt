import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.featherquest.R
import com.google.firebase.database.*

class AchievementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the userName from the intent
        val userName = intent.getStringExtra("userName") ?: ""

        setContent {
            AchievementsPage(userName = userName)
        }
    }
}

@Composable
fun AchievementsPage(userName: String) {
    val totalObservations = remember { mutableStateOf(0) }

    // Retrieve total observations for the user from Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("users/$userName/observations")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Count the number of observation entries
                totalObservations.value = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Achievements & Badges",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AchievementItem(
            iconId = R.drawable.ic_badge_novice,
            title = "Novice Observer",
            description = "Awarded for 1+ observations",
            earned = totalObservations.value >= 1
        )
        AchievementItem(
            iconId = R.drawable.ic_badge_intermediate,
            title = "Intermediate Observer",
            description = "Awarded for 10+ observations",
            earned = totalObservations.value >= 10
        )
        AchievementItem(
            iconId = R.drawable.ic_badge_expert,
            title = "Expert Observer",
            description = "Awarded for 50+ observations",
            earned = totalObservations.value >= 50
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Observations: ${totalObservations.value}",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun AchievementItem(
    iconId: Int,
    title: String,
    description: String,
    earned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = iconId),
            contentDescription = title,
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (earned) Color(0xFFD4AF37) else Color.Gray
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            // Display message for earned achievement
            if (earned) {
                Text(
                    text = "You earned this achievement!",
                    fontSize = 14.sp,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
