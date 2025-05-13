package jco.jcosaprfclink.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jco.jcosaprfclink.type.ErrorCode;

@Getter
@Setter
@NoArgsConstructor
public class ResponseResult {

	private String result;

	@Builder
	public ResponseResult(String result, ErrorCode errorCode) {
		if (errorCode==null) {
			this.result = result;
		} else {
			this.result = result + " : " + errorCode.getDescription();
		}
	}
}