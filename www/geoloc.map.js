(function (window, undefined) {
    this.Geoloc = {};
    'use strict';

    Geoloc.Map = function (opt) {

        if (!(this instanceof Geoloc.Map)) {
            return new Geoloc.Map(opt);
        }


        this.map = new ol.Map({
            layers: [
                new ol.layer.Tile({
                    source : new ol.source.OSM({
                        url: 'http://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png'
                    })
                })],
            target: "map",
            view: new ol.View({
                center: [430242.6629407953, 5411582.753012053],
                zoom: 9
            }),
            controls: new ol.Collection()

        });



        this.watchedPosition = function (position) {
            var point = ol.proj.transform([position.coords.longitude, position.coords.latitude], 'EPSG:4326','EPSG:3857');
            this.featuresOverlay = this.drawMarker(point, position.coords.accuracy);

        };

        this.drawMarker = function (lonLat, accurancy) {

            if (this.featuresOverlay !=null ){
                this.featuresOverlay.getSource().clear();
            }else{
                this.map.getView().setZoom(15);
            }
            this.map.getView().setCenter(lonLat);
            var positionFeature = new ol.Feature();
            positionFeature.setStyle(new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 6,
                    fill: new ol.style.Fill({
                        color: '#3399CC'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#fff',
                        width: 2
                    })
                })
            }));
            positionFeature.setGeometry(new ol.geom.Point(lonLat));
            var accurrancy = ol.geom.Polygon.circular(new ol.Sphere(6378137),  ol.proj.transform(lonLat,'EPSG:3857','EPSG:4326') , accurancy, 200).transform('EPSG:4326', 'EPSG:3857');
            //var accurrancy = ol.geom.Polygon.circular(new ol.Sphere(6378137), lonLat, accurancy);
            //accurrancy.applyTransform();
            var accuracyFeature = new ol.Feature();
            accuracyFeature.setGeometry(accurrancy);
            var featuresOverlay = new ol.layer.Vector({
                map: this.map,
                source: new ol.source.Vector({
                    features: [positionFeature, accuracyFeature]
                })
            });
            return featuresOverlay;
        };
    }


    Geoloc.Map.prototype.watchGPSPosition = function () {
        this.watchId = document(this.watchedPosition);
    }



})( this );