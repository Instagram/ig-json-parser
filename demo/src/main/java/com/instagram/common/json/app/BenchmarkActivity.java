// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.app;

import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64InputStream;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instagram.jsonbenchmark.app.R;
import com.instagram.common.json.app.igmodel.IgListOfModels;
import com.instagram.common.json.app.igmodel.IgModelRequest;
import com.instagram.common.json.app.igmodel.IgModelWorker;
import com.instagram.common.json.app.ommodel.OmListOfModels;
import com.instagram.common.json.app.ommodel.OmModelRequest;
import com.instagram.common.json.app.ommodel.OmModelWorker;

public class BenchmarkActivity extends Activity {
  private String mJsonString;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    try {
      mJsonString = loadFromFile(R.raw.input);
    } catch (IOException e) {
      Toast.makeText(this, "yeah, bad things happened", Toast.LENGTH_LONG)
          .show();
      return;
    }

    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean useIgParser = (view == findViewById(R.id.ig_parse_button));
        boolean useOmParser = (view == findViewById(R.id.om_parse_button));

        BenchmarkStats bs = new BenchmarkStats();

        int iterations = getIterationCount();
        if (iterations == 1) {
          IgModelRequest igModel;
          OmModelRequest omModel;
          try {
            bs.before();
            if (useIgParser) {
              igModel = new IgModelWorker().parseFromString(mJsonString);
            } else if (useOmParser) {
              omModel = new OmModelWorker().parseFromString(mJsonString);
            }
            bs.after();
          } catch (IOException ex) {
            Toast.makeText(BenchmarkActivity.this, "yeah, bad things happened", Toast.LENGTH_LONG)
                .show();
            return;
          }
        } else {
          String multiIterationInputString = generateInputString(iterations);
          IgListOfModels igListofModels;
          OmListOfModels omListofModels;
          try {
            bs.before();
            if (useIgParser) {
              igListofModels = new IgModelWorker().parseListFromString(multiIterationInputString);
            } else if (useOmParser) {
              omListofModels = new OmModelWorker().parseListFromString(multiIterationInputString);
            }
            bs.after();
          } catch (IOException ex) {
            Toast.makeText(BenchmarkActivity.this, "yeah, bad things happened", Toast.LENGTH_LONG)
                .show();
            return;
          }
        }

        ((TextView) findViewById(R.id.results)).setText(bs.renderResultsToText());
      }
    };

    findViewById(R.id.ig_parse_button).setOnClickListener(listener);
    findViewById(R.id.om_parse_button).setOnClickListener(listener);

    findViewById(R.id.quit).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        System.exit(0);
      }
    });
  }

  private String generateInputString(int iterations) {
    StringBuilder sb = new StringBuilder();

    sb.append("{\"list\": [");

    for (int ix = 0; ix < iterations; ix ++) {
      if (ix != 0) {
        sb.append(",");
      }
      sb.append(mJsonString);
    }

    sb.append("]}");

    return sb.toString();
  }

  private String loadFromFile(int resourceId) throws IOException {
    InputStreamReader inputStreamReader = null;

    try {
      // we're doing this absurd thing with encoding the json file in base64 because phabricator
      // chokes on it otherwise.
      inputStreamReader =
          new InputStreamReader(
              new Base64InputStream(getResources().openRawResource(resourceId), Base64.DEFAULT),
              "UTF-8");
      StringBuilder sb = new StringBuilder();
      char[] buffer = new char[8 * 1024];
      int bytesRead;

      while ((bytesRead = inputStreamReader.read(buffer)) != -1) {
        sb.append(buffer, 0, bytesRead);
      }

      return sb.toString();
    } finally {
      try {
        if (inputStreamReader != null) {
          inputStreamReader.close();
        }
      } catch (IOException ignored) {
        //ignored
      }
    }
  }

  int getIterationCount() {
    return Integer.valueOf(((EditText) findViewById(R.id.iterations)).getText().toString());
  }
}
