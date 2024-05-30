import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const customImageView = "customImageView";

class CustomImageWidget extends StatefulWidget {
  const CustomImageWidget({super.key, required this.imageUrl});

  final String imageUrl;

  @override
  State<CustomImageWidget> createState() => _CustomImageWidgetState();
}

class _CustomImageWidgetState extends State<CustomImageWidget> {
  final Map<String, dynamic> creationParams = <String, dynamic>{};
  late Key _key;

  @override
  void initState() {
    super.initState();
    _key = UniqueKey();
    creationParams['imageUrl'] = widget.imageUrl;
  }

  @override
  void didUpdateWidget(covariant CustomImageWidget oldWidget) {
    if (oldWidget.imageUrl != widget.imageUrl) {
      creationParams['imageUrl'] = widget.imageUrl;
      _key = UniqueKey();
    }
    super.didUpdateWidget(oldWidget);
  }

  @override
  Widget build(BuildContext context) {
    return Platform.isAndroid
        ? AndroidView(
            key: _key,
            viewType: customImageView,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
          )
        : UiKitView(
            viewType: customImageView,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
            key: _key,
          );
  }
}
