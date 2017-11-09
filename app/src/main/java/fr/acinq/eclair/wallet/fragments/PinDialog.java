package fr.acinq.eclair.wallet.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import fr.acinq.eclair.wallet.R;

public class PinDialog extends Dialog {

  private static final String TAG = "PinDialog";
  private TextView mPinTitle;
  private TextView mPinDisplay;
  private List<View> mButtonsList = new ArrayList<>();
  private PinDialogCallback mPinCallback;

  public PinDialog(final Context context, final int themeResId, final @NotNull PinDialogCallback pinCallback) {
    this(context, themeResId, pinCallback, context.getString(R.string.pindialog_title_default));
  }

  public PinDialog(final Context context, final int themeResId, final @NotNull PinDialogCallback pinCallback, final String title) {
    super(context, themeResId);

    // callback must be defined
    mPinCallback = pinCallback;

    // layout
    setContentView(R.layout.dialog_pin);

    setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        mPinCallback.onPinCancel(PinDialog.this);
      }
    });

    // set up pin numpad
    mPinTitle = findViewById(R.id.pin_title);
    mPinTitle.setText(title);
    mPinDisplay = findViewById(R.id.pin_display);

    mButtonsList.add(findViewById(R.id.pin_num_2));
    mButtonsList.add(findViewById(R.id.pin_num_1));
    mButtonsList.add(findViewById(R.id.pin_num_3));
    mButtonsList.add(findViewById(R.id.pin_num_4));
    mButtonsList.add(findViewById(R.id.pin_num_5));
    mButtonsList.add(findViewById(R.id.pin_num_6));
    mButtonsList.add(findViewById(R.id.pin_num_7));
    mButtonsList.add(findViewById(R.id.pin_num_8));
    mButtonsList.add(findViewById(R.id.pin_num_9));
    mButtonsList.add(findViewById(R.id.pin_num_0));

    for (View v : mButtonsList) {
      v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (mPinDisplay.getText() == null || mPinDisplay.getText().length() < 6) {
            String val = ((Button) view).getText().toString();
            mPinDisplay.setText(mPinDisplay.getText() + val);
          }
        }
      });
    }
    findViewById(R.id.pin_num_clear).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPinDisplay.setText("");
      }
    });
    findViewById(R.id.pin_submit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mPinCallback.onPinConfirm(PinDialog.this, mPinDisplay.getText().toString());
      }
    });
  }

  public void animateSuccess() {
    this.dismiss();
  }

  public void animateFailure() {
    this.dismiss();
  }

  public interface PinDialogCallback {
    void onPinConfirm(final PinDialog dialog, final String pinValue);

    void onPinCancel(final PinDialog dialog);
  }
}
