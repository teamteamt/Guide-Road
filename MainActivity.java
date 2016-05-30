package com.example.guideroads;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener,
        MapView.MapViewEventListener {
    public static final String API_KEY = "cde309b8cc7387c67fa231ef0287476b";
    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();

    MapView mMapView = null;
    EditText my_location;
    EditText my_destination;
    Button Search;
    Button roadservice;
    Geocoder coder;


    class MyListenerClass implements View.OnClickListener {
        public void onClick(View v) {
            String query = my_destination.getText().toString();
            if (query == null || query.length() == 0) {
                showToast("검색어가 입력되지 않았습니다.");
                return;
            }
            hideSoftKeyboard(); // 키보드 숨김
            MapPoint.GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord();
            double latitude = geoCoordinate.latitude; // 위도
            double longitude = geoCoordinate.longitude; // 경도
            int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
            int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개

            Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
            searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, API_KEY, new OnFinishSearchListener() {

                @Override
                public void onSuccess(List<Item> itemList) {
                    mMapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                    showResult(itemList); // 검색 결과 보여줌
                }

                @Override
                public void onFail() {
                    Toast.makeText(getApplicationContext(), "API_KEY의 제한 트래픽이 초과되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class DaumRoadservice implements View.OnClickListener
    {
        public void onClick(View v)
        {
            List<Address> start_location = null;
           // List<Address> destinaion = null;   // 자신이 직접 목적지를 입력하고싶을때 사용 (주석처리한것들이 모두 사용하기위해 필요함)
            String local = my_location.getText().toString();
            String desti = my_destination.getText().toString();
            double sta_Latitude;
            double sta_Longitude;
            //double des_Latitude;
            //double des_Longitude;

            if (desti == null || desti.length() == 0) {
                showToast("검색어가 입력되지 않았습니다.");
                return;
            }

            try{
                start_location = coder.getFromLocationName(local,5);
                //destinaion = coder.getFromLocationName(desti,5);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "입출력오류 :" + e.getMessage() +"", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            sta_Latitude = start_location.get(0).getLatitude();
            sta_Longitude = start_location.get(0).getLongitude();
            //des_Latitude = destinaion.get(0).getLatitude();
            //des_Longitude = destinaion.get(0).getLongitude();


            MapPoint.GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord(); // 아이콘 누르고 목적지 생성

            double latitude = geoCoordinate.latitude; // 위도
            double longitude = geoCoordinate.longitude; // 경도

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            //intent.setData(Uri.parse("daummaps://route?sp="+sta_Latitude+","+sta_Longitude+"&ep="+des_Latitude+","+des_Longitude+"&by=FOOT")); //자신이 직접 목적지 설정
            intent.setData(Uri.parse("daummaps://route?sp=" + sta_Latitude + "," + sta_Longitude + "&ep=" + latitude + "," + longitude + "&by=FOOT")); // 자신이 누른 아이콘으로 목적지 설정
            startActivity(intent);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.Dmap);  // mMapView에 Dmap(다음지도)를 넣음
        mMapView.setDaumMapApiKey(API_KEY);  // API키 셋팅
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        coder = new Geocoder(getApplicationContext(), Locale.KOREA);
        my_location = (EditText) findViewById(R.id.start_edit);
        my_destination = (EditText) findViewById(R.id.destination_edit); // 검색창
        Search = (Button) findViewById(R.id.search); // 검색버튼
        roadservice = (Button) findViewById(R.id.road);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                List<Address> list = null;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);  // MARKER의 위치 설정

                try // 역지오 코딩 (위도,경도 -> 주소로 변환)
                {
                    list = coder.getFromLocation(Double.parseDouble(String.valueOf(latitude)), Double.parseDouble(String.valueOf(longitude)), 1);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    my_location.setText("입출력오류 : " + e.getMessage());
                    e.printStackTrace();
                }
                if (list != null)
                    my_location.setText(list.get(0).getAddressLine(0).toString()); // getAddressLine(0)는 주소라인만 출력하기 위한용도

                mMapView.removeAllPOIItems(); // 오버레이된 POIitem 을 모두 지워주기 위해 선언.

                MapPOIItem marker = new MapPOIItem();

                marker.setItemName("현 위치");
                marker.setTag(1);
                mMapView.setMapCenterPointAndZoomLevel(MARKER_POINT, 1, true); // 중심점 잡기
                mMapView.zoomIn(true);  // 줌 인
                mMapView.zoomOut(true);  // 줌 아웃

                marker.setMapPoint(MARKER_POINT); // 마커 생성위치 설정
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setCustomImageResourceId(R.drawable.map_pin_blue);
                //marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);  //마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

                mMapView.addPOIItem(marker); // 마커 그려주기

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 3, locationListener);

        MyListenerClass buttonListener = new MyListenerClass();
        Search.setOnClickListener(buttonListener);
        DaumRoadservice buttonListener2 = new DaumRoadservice();
        roadservice.setOnClickListener(buttonListener2);

    }


    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void showResult(List<Item> itemList) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);  // 커스텀 이미지 지정
            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_red); // 선택시 아이콘 이미지변경
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);
            // mMapView.setZoomLevel(2, true); // 줌 level 설정
        }

        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

        MapPOIItem[] poiItems = mMapView.getPOIItems();
        if (poiItems.length > 0) {
            mMapView.selectPOIItem(poiItems[0], false);
        }
    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(my_destination.getWindowToken(), 0);
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {
    }
}
