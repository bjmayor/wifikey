/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <string.h>
#include <jni.h>
#include<android/log.h>

jcharArray short2jchartarray(JNIEnv* env, short *btPath, int str_len);
short* jcharArray2short(JNIEnv* env, jcharArray aInput);
short* HloveyRC4_jarray(JNIEnv* env, jcharArray aInput); //, jcharArray akey);
short* HloveyRC4_carray(JNIEnv* env, short *iInputChar, int str_len);
short* code_in_c(short *iInputChar, short *aKey, int input_char_length,
		int akey_length);
char bin2hex(char input);
char hex2bin(char input);

jcharArray Java_com_hiwifi_utils_encode_Security_stringFromJNI(JNIEnv* env,
		jobject obj, jcharArray str) { //, jcharArray key) {
	int str_len = (*env)->GetArrayLength(env, str);
	jcharArray ret = short2jchartarray(env, HloveyRC4_jarray(env, str),
			str_len); //, key);
	return ret;
}

jcharArray Java_com_hiwifi_utils_encode_Security_stringDecodeJNI(JNIEnv* env,
		jobject obj, jcharArray str) {
	int str_len = (*env)->GetArrayLength(env, str);
	int decode_len = str_len >> 1;
	int ind = 0;
	int i = 0;
	unsigned
	short* code = jcharArray2short(env, str);
	unsigned
	short rc4[decode_len];
	for (i = 0; i < str_len; i += 2) {
		rc4[ind++] = ((hex2bin((char) code[i]) << 4)
				| hex2bin((char) code[i + 1]));
	}
	unsigned short* decode = HloveyRC4_carray(env, rc4, decode_len);
	unsigned short ret[decode_len];
	ind = 0;
	int max = (str_len >> 1);
	for (i = 0; i < max; i += 1) {
		unsigned short d1 = decode[i];
		unsigned short d2 = d1 >> 5;
		if ((d1 >> 7) == 0) {
			ret[ind++] = d1;
		} else if (d2 == 0x6) {
			unsigned short t1 = d1 & 0x1f;
			unsigned short t2 = decode[i + 1] & 0x3f;
			ret[ind++] = (unsigned short) ((t1 << 6) | t2);
			i += 1;
			decode_len--;
		} else {
			unsigned short t1 = decode[i + 1] & 0x3f;
			unsigned short t2 = decode[i + 2] & 0x3f;
			ret[ind++] = (unsigned short) (((d1 & 0xf) << 12) | (t1 << 6) | t2);
			i += 2;
			decode_len -= 2;
		}
	}
	return short2jchartarray(env, ret, decode_len);
}

jcharArray Java_com_hiwifi_utils_encode_Security_stringEncodeJNI(JNIEnv* env,
		jobject obj, jcharArray str) {
	int str_len = (*env)->GetArrayLength(env, str);
	int encode_len = 0;
	int ind = 0;
	int i = 0;
	unsigned short *code = jcharArray2short(env, str);
	for (i = 0; i < str_len; i++) {
		unsigned short temp = code[i];
		if (temp < 0x80) {
			encode_len += 1;
		} else if (temp < 0x800) {
			encode_len += 2;
		} else {
			encode_len += 3;
		}
	}
	unsigned short utf8[encode_len];
	for (i = 0; i < str_len; i++) {
		unsigned short temp = code[i];
		if (temp < 0x80) {
			utf8[ind++] = temp & 0x7f;
		} else if (temp < 0x800) {
			utf8[ind++] = ((temp >> 6) & 0x01f) | 0x60;
			utf8[ind++] = (temp & 0x03f) | 0x80;
		} else {
			utf8[ind++] = ((temp >> 12) & 0xf) | 0xe0;
			utf8[ind++] = ((temp >> 6) & 0x3f) | 0x80;
			utf8[ind++] = (temp & 0x3f) | 0x80;
		}
	}
	ind = 0;
	unsigned short* rc4 = HloveyRC4_carray(env, utf8, encode_len);
	int ret_len = encode_len << 1;
	unsigned short ret[ret_len];
	for (i = 0; i < encode_len; i += 1) {
		unsigned short tt = rc4[i];
		ret[ind++] = bin2hex((char) (tt >> 4) & 0xf);
		ret[ind++] = bin2hex((char) (tt & 0xf));
	}
	return short2jchartarray(env, ret, ret_len);
}

char hex2bin(char input) {
	if (input >= 'A' && input <= 'F') {
		return (char) (input - 'A' + 10);
	} else if (input >= '0' && input <= '9') {
		return (char) (input - '0');
	} else {
		return 0;
	}
}

char bin2hex(char input) {
	if (input > 9 && input < 16) {
		return (char) ('A' + input - 10);
	} else if (input >= 0) {
		return (char) ('0' + input);
	} else {
		return 0;
	}
}

short* HloveyRC4_carray(JNIEnv* env, short *iInputChar, int str_len) {
	int key_len = 11;
	short aKey[key_len];
	aKey[0] = 'k';
	aKey[1] = 'z';
	aKey[2] = 'h';
	aKey[3] = '1';
	aKey[4] = '9';
	aKey[5] = '8';
	aKey[6] = '9';
	aKey[7] = '1';
	aKey[8] = '2';
	aKey[9] = '0';
	aKey[10] = '8';
	return code_in_c(iInputChar, aKey, str_len, key_len);
}

short* HloveyRC4_jarray(JNIEnv* env, jcharArray aInput) { //, jcharArray akey) {
	int str_len = (*env)->GetArrayLength(env, aInput); // get the array in c
	//	int key_len = (*env)->GetArrayLength(env, akey);
	//short *aKey = jcharArray2short(env, akey);
	short *iInputChar = jcharArray2short(env, aInput);
	return HloveyRC4_carray(env, iInputChar, str_len); //calculate the char array
}

jcharArray short2jchartarray(JNIEnv* env, short *btPath, int str_len) {
	jcharArray RtnArr = NULL; //change the array in C to array in Java
	RtnArr = (*env)->NewCharArray(env, str_len);
	(*env)->SetCharArrayRegion(env, RtnArr, 0, str_len, (jchar*) btPath);
	return RtnArr;
}

short* jcharArray2short(JNIEnv* env, jcharArray aInput) {
	short *iInputChar = 0;
	short i = 0;
	jshort len = (*env)->GetArrayLength(env, aInput);
	iInputChar = (short *) malloc(len * 2 + 1);
	if (iInputChar == 0) {
		(*env)->DeleteLocalRef(env, aInput);
		return 0;
	}
	(*env)->GetCharArrayRegion(env, aInput, 0, len, (jshort *) iInputChar);
	return iInputChar;
}

short* code_in_c(short *iInputChar, short *aKey, int input_char_length,
		int akey_length) {
	short iS[256];
	short iK[256];
	short i;
	short si;
	short j;
	short iOutputChar[input_char_length];
	for (i = 0; i < 256; i++) {
		iS[i] = i;
		iK[i] = aKey[i % akey_length];
	}
	j = 0;
	for (i = 0; i < 255; i++) {
		j = (j + iS[i] + iK[i]) % 256;
		short temp = iS[i];
		iS[i] = iS[j];
		iS[j] = temp;
	}
	i = 0;
	j = 0;
	si = 0;
	for (si = 0; si < input_char_length; si++) {
		i = (i + 1) % 256;
		j = (j + iS[i]) % 256;
		short temp = iS[i];
		iS[i] = iS[j];
		iS[j] = temp;
		short t = (iS[i] + iS[j]) % 256;
		short iY = iS[t];
		iOutputChar[si] = (short) (iInputChar[si] ^ iY);
	}
	return iOutputChar;
}
