
function FeatureLoader(url,layer,extraURLParameters)
{
    this.url=url;
    this.layer=layer;
    this.extraURLParameters = extraURLParameters;
    this.xhr2 = new XMLHttpRequest();
	this.xhr2.addEventListener("load", this.processFeatures.bind(this));
    this.indexedFeatures = new Array();
}

FeatureLoader.prototype.loadFeatures=function(bounds)
{
    var qs ='bbox=' +
            bounds.getSouthWest().lng+ ',' + 
            bounds.getSouthWest().lat+ ',' + 
            bounds.getNorthEast().lng+ ',' + 
            bounds.getNorthEast().lat + '&' + this.extraURLParameters;
    this.xhr2.open ("GET", this.url + "?" + qs);
	this.xhr2.send();
}

FeatureLoader.prototype.processFeatures=function(e)
{
	var json=JSON.parse(e.target.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        var c = json.features[i].geometry.coordinates;
        if (!this.indexedFeatures[json.features[i].properties.id])
        {
            this.indexedFeatures[json.features[i].properties.id]=
                    json.features[i];
            this.layer.addData(json.features[i]);
        }
    }
}
