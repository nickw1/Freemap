// brtimes.com
// www.brtimes.com/#!board?stn=XXX&date=YYYYMMDD

function InformationFormatter (feature) {
    this.feature = feature;
}

InformationFormatter.prototype.format = function() {
    var html = this.feature.properties.name ? "<h2>"+
            this.feature.properties.name + "</h2>" : "";
    html += "<p>";
    html += "<em>"+this.feature.properties.featuretype+"</em></p><p>";

	if(this.feature.properties.description) {
		html += "<p>" + this.feature.properties.description + "</p>";
	}

    if(this.feature.properties.wikipedia) {
        html += "Wikipedia article <a target='_tab2' "+
            "href='http://www.wikipedia.org/wiki/"+
            this.feature.properties.wikipedia + "'>here</a>.<br />";
    }
    if(this.feature.properties.website) {
        html += "Website <a target='_tab2' href='" +
            this.feature.properties.website + "'>here</a>.<br />";
    }

    switch(this.feature.properties.featuretype) {
        case 'pub':
            if(this.feature.properties.real_ale == 'yes') {
                html +=  "<em>This pub has real ale.</em><br />";
            }
            break;

        case 'station':
            if(this.feature.properties.ref) {
                html += "See train times for today from this station "+
                    "<a target='_tab2' "+
                    "href='http://www.brtimes.com/#!board?stn=" +
                    this.feature.properties.ref+"'>here</a>.<br />";
            }
            break;
    }
    html += "</ul>";
    return html;
}
