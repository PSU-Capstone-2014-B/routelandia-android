/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package edu.pdx.its.portal.routelandia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ListStat extends Activity {

    private Button mapbtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_stats);

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        mapbtn = (Button) findViewById(R.id.map);
        mapbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //ShowDialog(DATE_DIALOG_ID);
                //Save date in jason array and switch to map for now...
                Log.i("clicks", "you clicked start");
                Intent i = new Intent(
                        ListStat.this,
                        MapsActivity.class);
                startActivity(i);
            }
        });
    }


}