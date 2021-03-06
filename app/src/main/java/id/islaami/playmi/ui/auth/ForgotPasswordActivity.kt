package id.islaami.playmi.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import id.islaami.playmi.R
import id.islaami.playmi.ui.base.BaseActivity
import id.islaami.playmi.util.ui.setVisibilityToGone
import id.islaami.playmi.util.ui.setVisibilityToVisible
import id.islaami.playmi.util.ui.showLongToast
import kotlinx.android.synthetic.main.forgot_password_activity.*

class ForgotPasswordActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        btnSend.setOnClickListener {
            btnSend.setVisibilityToGone()
            progressBar.setVisibilityToVisible()

            FirebaseAuth.getInstance().sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener { task ->
                    btnSend.setVisibilityToVisible()
                    progressBar.setVisibilityToGone()

                    if (task.isSuccessful) {
                        layoutField.setVisibilityToGone()
                        textTitle.text = "Cek Email Anda!"
                        textSubtitle.text =
                            "Instruksi pengaturan ulang kata sandi\nsudah dikirimkan ke email Anda"
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            showLongToast(
                                getString(
                                    R.string.error_email_not_found,
                                    email.text.toString()
                                )
                            )
                        } catch (e: Exception) {
                            showLongToast(getString(R.string.error_message_default))
                        }
                    }
                }
        }

        linkLogin.setOnClickListener { onBackPressed() }
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
        }
    }
}
