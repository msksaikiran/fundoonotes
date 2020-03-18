package com.bridgelabz.fundoonote.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class NoteResponse {
	private String Message;
	private Object notes;
	private int statusCode;
	
	

}