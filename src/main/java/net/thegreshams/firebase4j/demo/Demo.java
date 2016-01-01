package net.thegreshams.firebase4j.demo;

import java.io.IOException;

import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

public class Demo {

	public static void main(final String[] args) throws IOException {

		// URL is passed in as argument, e.g.
		// $ java Demo http://gama.firebase.com/team
		String baseUrl = args[0];

		// create the firebase
		Firebase firebase = new Firebase(baseUrl);

		FirebaseResponse putResponse = firebase
				.addData("department", "Accounting")
				.put();
		System.out.printf("\n\nResult of PUT to %s:\n%s\n", putResponse.getUrl(), putResponse);
		// Prints:
		// Result of PUT to http://gama.firebase.com/team:
		// FirebaseResponse(success=true, code=200, rawBody={"department":"Accounting"}, url=https://gama.firebase.com/team/.json?, body={department=Accounting})

		FirebaseResponse postResponse = firebase
				.addData("director", "Joshua")
				.post("/management");
		System.out.printf("\n\nResult of POST to %s:\n%s\n", postResponse.getUrl(), postResponse);
		// Prints:
		// Result of POST to http://gama.firebase.com/team/management:
		// FirebaseResponse(success=true, code=200, rawBody={"name":"-K6z9NnyVvLCAyz3UgM8"}, url=https://gama.firebase.com/management.json?, body={name=-K6z9NnyVvLCAyz3UgM8})

		FirebaseResponse deleteResponse = firebase.delete("/management");
		System.out.printf("\n\nResult of DELETE to %s:\n%s\n", deleteResponse.getUrl(), deleteResponse);
		// Prints:
		// Result of DELETE to http://gama.firebase.com/team/management:
		// FirebaseResponse(success=false, code=200, rawBody=null, url=https://gama.firebase.com/management.json?, body=null)

		FirebaseResponse getResponse = firebase.get();
		System.out.printf("\n\nResult of GET to %s:\n%s\n", getResponse.getUrl(), getResponse);
		// Prints:
		// Result of DELETE to http://gama.firebase.com/team/management:
		// FirebaseResponse(success=true, code=200, rawBody={"department":"Accounting","management":{"director":"Joshua"}}, url=https://gama.firebase.com/.json?, body={department=Accounting, management={director=Joshua}})

		FirebaseResponse getResponse2 = firebase.get("management");
		System.out.printf("\n\nResult of GET to %s:\n%s\n", getResponse2.getUrl(), getResponse2);
		// Prints:
		// Result of GET to http://gama.firebase.com/team/management:
		// FirebaseResponse(success=true, code=200, rawBody={"director":"Joshua"}, url=https://gama.firebase.com/management.json?, body={director=Joshua})

		FirebaseResponse postResponse1 = firebase
				.addData("Joe", "24")
				.addData("Sam", "36")
				.addData("Annie", "27")
				.post("personal/ages");
		System.out.printf("\n\nResult of POST to %s:\n%s\n", postResponse1.getUrl(), postResponse1);
		// Prints:
		// Result of POST to http://gama.firebase.com/team/personal/management:
		// FirebaseResponse(success=true, code=200, rawBody={"name":"-K6z9NrMg6JxIlzh99sf"}, url=https://gama.firebase.com/personal/ages.json?, body={name=-K6z9NrMg6JxIlzh99sf})

		FirebaseResponse getResponse3 = firebase.get("personal/ages");
		System.out.printf("\n\nResult of GET to %s:\n%s\n", getResponse3.getUrl(), getResponse3);
		// Prints:
		// Result of DELETE to http://gama.firebase.com/team/personal/ages:
		// FirebaseResponse(success=true, code=200, rawBody={"-K6z9NrMg6JxIlzh99sf":{"Annie":"27","Joe":"24","Sam":"36"}}, url=https://gama.firebase.com/personal/ages.json?, body={-K6z9NrMg6JxIlzh99sf={Annie=27, Joe=24, Sam=36}})
	}
}
