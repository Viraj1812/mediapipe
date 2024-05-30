import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mediapipe/custom_image_view.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  var imageUrl = '';
  static const pickImageChannnel = MethodChannel('pickImagePlatform');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () async {
                  try {
                    final result = await pickImageChannnel.invokeMethod('pickImage');
                    setState(() {
                      imageUrl = result;
                    });
                  } on PlatformException catch (e) {
                    log('Failed to pick image: ${e.message}');
                  }
                },
                child: const Text('Pick Image'),
              ),
              const SizedBox(height: 20),
              imageUrl != ''
                  ? SizedBox(
                      width: 400,
                      height: 400,
                      child: CustomImageWidget(imageUrl: imageUrl),
                    )
                  : const SizedBox.shrink(),
            ],
          ),
        ),
      ),
    );
  }
}
