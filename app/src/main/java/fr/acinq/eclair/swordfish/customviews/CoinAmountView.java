package fr.acinq.eclair.swordfish.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import fr.acinq.bitcoin.Satoshi;
import fr.acinq.bitcoin.package$;
import fr.acinq.eclair.swordfish.R;
import fr.acinq.eclair.swordfish.utils.CoinFormat;
import scala.math.BigDecimal;

public class CoinAmountView extends LinearLayout {
  private String unit;
  private TextView amountTextView;
  private TextView unitTextView;
  private Satoshi amountSat = new Satoshi(0);

  public CoinAmountView(Context context) {
    super(context);
    init(null, 0);
  }

  public CoinAmountView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public CoinAmountView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs, defStyle);
  }

  private void init(AttributeSet attrs, int defStyle) {

    final TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.CoinAmountView, defStyle, 0);
    try {
      String service = Context.LAYOUT_INFLATER_SERVICE;
      LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
      LinearLayout layout = (LinearLayout) li.inflate(R.layout.coin_amount_view, this, true);
      amountTextView = (TextView) layout.findViewById(R.id.view_amount);
      unitTextView = (TextView) layout.findViewById(R.id.view_unit);


      int amount_size = arr.getDimensionPixelSize(R.styleable.CoinAmountView_amount_size, 0);
      int amount_color = arr.getColor(R.styleable.CoinAmountView_amount_color, 0);
      amountTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, amount_size);
      amountTextView.setTextColor(amount_color);
      amountTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

      unit = arr.getString(R.styleable.CoinAmountView_unit);
      int unit_size = arr.getDimensionPixelSize(R.styleable.CoinAmountView_unit_size, 0);
      int unit_color = arr.getColor(R.styleable.CoinAmountView_unit_color, 0);
      unitTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, unit_size);
      unitTextView.setTextColor(unit_color);
      unitTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
      unitTextView.setText(unit);
    } finally {
      arr.recycle();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (this.unit) {
      case "BTC":
        setUnit("mBTC");
        break;
      case "mBTC":
        setUnit("BTC");
        break;
      default:
        setUnit("BTC");
    }
    return super.onTouchEvent(event);
  }

  public void setAmountSat(Satoshi amountSat) {
    this.amountSat = amountSat;
    refreshView();
  }

  public void setUnit(String unit) {
    this.unit = unit;
    unitTextView.setText(this.unit);
    refreshView();
  }

  private void refreshView() {
    switch (this.unit) {
      case "BTC":
        BigDecimal amount_btc = package$.MODULE$.satoshi2btc(amountSat).amount();
        amountTextView.setText(CoinFormat.getBTCFormat().format(amount_btc));
        break;
      case "mBTC":
        BigDecimal amount_mbtc = package$.MODULE$.satoshi2millibtc(amountSat).amount();
        amountTextView.setText(CoinFormat.getMilliBTCFormat().format(amount_mbtc));
        break;
      default:
        amountTextView.setText(NumberFormat.getInstance().format(amountSat.amount()));
    }
    invalidate();
    requestLayout();
  }
}