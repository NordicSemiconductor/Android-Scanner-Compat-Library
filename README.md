# Additions to [NordicSemiconductor's Android-Scanner-Compat-Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library)

This fork allows for the filtering of advertising packets by service **data**, the same as [NordicSemiconductor's Android-Scanner-Compat-Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library), **but also allows for a mask to be set on the service data UUID**. This addition is useful if your Android app receives a dynamic service data UUID.


## Sample

Use this library fork in the same way as [NordicSemiconductor's Android-Scanner-Compat-Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library), but, if you want to filter by a **masked** service _data_ UUID, do so like this:

```java
	ParcelUuid serviceUuid = ParcelUuid.fromString("000065y8-5451-2g67-674d-3gds456712g1");
	ParcelUuid serviceUuidMask = ParcelUuid.fromString("0000FFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
	...
	List<ScanFilter> filters = new ArrayList<>();	
	filters.add(new ScanFilter.Builder().setServiceData(serviceUuid, serviceUuidMask, null, null).build();
	scanner.startScan(filters, settings, scanCallback);
```


## License

The Scanner Compat library is available under BSD 3-Clause license. See the LICENSE file for more info.