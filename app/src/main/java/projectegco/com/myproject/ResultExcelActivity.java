package projectegco.com.myproject;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class ResultExcelActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_excel);
        setTitle("Result");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //set back button

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.128/upload/QueryDB.php"));
        startActivity(browserIntent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
