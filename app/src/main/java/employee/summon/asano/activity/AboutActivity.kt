package employee.summon.asano.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import employee.summon.asano.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import java.util.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        simulateDayNight(/* DAY */0)

        val version = packageManager.getPackageInfo(packageName, 0).versionName

        val aboutPage = AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.person_icon)
                .addItem(Element().setTitle(getString(R.string.version) + " " + version))
                .addEmail("ern.asanov@yandex.ru")
                .addItem(getCopyRightsElement())
                .setDescription(getString(R.string.description))
                .create()

        setContentView(aboutPage)
    }

    private fun getCopyRightsElement(): Element {
        val copyRightsElement = Element()
        val copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR))
        copyRightsElement.title = copyrights
        copyRightsElement.iconTint = mehdi.sakout.aboutpage.R.color.about_item_icon_color
        copyRightsElement.iconNightTint = android.R.color.white
        copyRightsElement.gravity = Gravity.CENTER
        copyRightsElement.onClickListener = View.OnClickListener {
            Toast.makeText(this@AboutActivity, copyrights, Toast.LENGTH_SHORT).show()
        }
        return copyRightsElement
    }

    private fun simulateDayNight(currentSetting: Int) {
        val day = 0
        val night = 1
        val followSystem = 3

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentSetting == day && currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO)
        } else if (currentSetting == night && currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES)
        } else if (currentSetting == followSystem) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
