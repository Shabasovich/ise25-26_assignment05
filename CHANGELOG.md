# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.0.2] - 2025-11-12

### Added

- Add Cucumber dependencies, test runner, and examples.
- Add `Dockerfile` and `compose.yaml` to allow interested students to run the application in a Docker container.
- Add Feign client to interact with OpenStreetMap API.
- Add functionality to fetch data from OSM nodes to `PosDataServiceImpl`.
- Add conversion of OSM data to POS entities to `PosServiceImpl`.

### Changed

- Fix Surfire configuration resulting in a warning.
- Use JRE instead of JDK base image in `Dockerfile` to reduce image size.
- Move test dependencies to `test` scope to reduce size of `application` JAR file.
- Modify OSM import endpoint to include campus type (enable implementation of assignment 4).

### Removed

- n/a

## [0.0.1] - 2025-11-04

### Added

- Add new `POST` endpoint `/api/pos/import/osm/{nodeId}` that allows API users to import a `POS` based on an OpenStreetMap node.
- Add example of new OSM import endpoint to `README` file.

### Changed

- Fix broken test case in `PosSystemTests` (assignment 3).
- Extend GitHub Actions triggers to include pushes to feature branches (assignment 3).
- Extend `PosService` interface by adding a `importFromOsmNode` method.

### Removed

- n/a
