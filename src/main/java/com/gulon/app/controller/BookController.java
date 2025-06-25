package com.gulon.app.controller;

import com.gulon.app.dto.BookDto;
import com.gulon.app.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "도서 관리", description = "도서 검색, 등록, 수정, 삭제 및 도서 정보 관리 API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookController {

    private final BookService bookService;

    @Operation(
        summary = "도서 검색 (네이버 API 통합)",
        description = "네이버 도서 검색 API를 통해 도서를 검색합니다. 검색어, 출력 개수, 시작 위치, 정렬 방식을 지정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공", 
                    content = @Content(schema = @Schema(implementation = BookDto.SearchResult.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<List<BookDto.SearchResult>> searchBooks(
            @Parameter(description = "검색할 도서명, 저자명 등", required = true, example = "자바의 정석")
            @RequestParam @NotBlank String query,
            @Parameter(description = "검색 결과 출력 개수 (1~100)", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) Integer display,
            @Parameter(description = "검색 시작 위치 (1~1000)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Integer start,
            @Parameter(description = "정렬 방식 (sim: 정확도순, date: 출간일순, count: 판매량순)", example = "sim")
            @RequestParam(defaultValue = "sim") String sort) {
        
        log.info("도서 검색 요청 - 검색어: {}, 개수: {}, 시작: {}", query, display, start);
        
        BookDto.SearchRequest request = new BookDto.SearchRequest();
        request.setQuery(query);
        request.setDisplay(display);
        request.setStart(start);
        request.setSort(sort);
        
        List<BookDto.SearchResult> results = bookService.searchBooks(request);
        
        return ResponseEntity.ok(results);
    }

    @Operation(
        summary = "도서 목록 조회 (페이징)",
        description = "등록된 도서 목록을 페이징하여 조회합니다. 페이지 번호, 페이지 크기, 정렬 기준을 지정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BookDto.BookListResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 페이징 파라미터")
    })
    @GetMapping
    public ResponseEntity<BookDto.BookListResponse> getBooks(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size,
            @Parameter(description = "정렬 기준 (createdAt, updatedAt, title 등)", example = "updatedAt")
            @RequestParam(defaultValue = "updatedAt") String sort) {
        
        log.info("도서 목록 조회 - 페이지: {}, 크기: {}", page, size);
        
        BookDto.BookListResponse response = bookService.getBooks(page, size, sort);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "도서 상세 조회",
        description = "공개 ID를 통해 특정 도서의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BookDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음")
    })
    @GetMapping("/{publicId}")
    public ResponseEntity<BookDto.Response> getBook(
            @Parameter(description = "도서 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("도서 상세 조회 - publicId: {}", publicId);
        
        return bookService.getBookByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "ISBN으로 도서 조회",
        description = "ISBN을 통해 도서 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ISBN의 도서를 찾을 수 없음")
    })
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookDto.Response> getBookByIsbn(
            @Parameter(description = "도서 ISBN (13자리)", required = true, example = "9788960777323")
            @PathVariable @NotBlank String isbn) {
        log.info("ISBN으로 도서 조회 - ISBN: {}", isbn);
        
        return bookService.getBookByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "도서 생성",
        description = "새로운 도서 정보를 데이터베이스에 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "도서 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 도서 정보"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 도서 (ISBN 중복)")
    })
    @PostMapping
    public ResponseEntity<BookDto.Response> createBook(
            @Parameter(description = "생성할 도서 정보", required = true)
            @Valid @RequestBody BookDto.CreateRequest request) {
        log.info("도서 생성 요청 - 제목: {}", request.getTitle());
        
        BookDto.Response response = bookService.createBook(request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "도서 수정",
        description = "기존 도서의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 수정 정보")
    })
    @PutMapping("/{publicId}")
    public ResponseEntity<BookDto.Response> updateBook(
            @Parameter(description = "수정할 도서의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "수정할 도서 정보", required = true)
            @Valid @RequestBody BookDto.UpdateRequest request) {
        
        log.info("도서 수정 요청 - publicId: {}", publicId);
        
        return bookService.updateBook(publicId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "도서 삭제",
        description = "지정된 도서를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "삭제할 도서의 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("도서 삭제 요청 - publicId: {}", publicId);
        
        boolean deleted = bookService.deleteBook(publicId);
        
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "로컬 도서 검색",
        description = "로컬 데이터베이스에서 키워드로 도서를 검색합니다."
    )
    @GetMapping("/search/local")
    public ResponseEntity<BookDto.BookListResponse> searchLocalBooks(
            @Parameter(description = "검색 키워드", required = true, example = "스프링")
            @RequestParam @NotBlank String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        
        log.info("로컬 도서 검색 - 키워드: {}", keyword);
        
        BookDto.BookListResponse response = bookService.searchBooksByKeyword(keyword, page, size);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "저자별 도서 목록",
        description = "특정 저자의 도서 목록을 조회합니다."
    )
    @GetMapping("/author/{author}")
    public ResponseEntity<BookDto.BookListResponse> getBooksByAuthor(
            @Parameter(description = "저자명", required = true, example = "이효석")
            @PathVariable @NotBlank String author,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        
        log.info("저자별 도서 조회 - 저자: {}", author);
        
        BookDto.BookListResponse response = bookService.getBooksByAuthor(author, page, size);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "출판사별 도서 목록",
        description = "특정 출판사의 도서 목록을 조회합니다."
    )
    @GetMapping("/publisher/{publisher}")
    public ResponseEntity<BookDto.BookListResponse> getBooksByPublisher(
            @Parameter(description = "출판사명", required = true, example = "한빛미디어")
            @PathVariable @NotBlank String publisher,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        
        log.info("출판사별 도서 조회 - 출판사: {}", publisher);
        
        BookDto.BookListResponse response = bookService.getBooksByPublisher(publisher, page, size);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "베스트셀러 목록",
        description = "베스트셀러 도서 목록을 조회합니다."
    )
    @GetMapping("/bestsellers")
    public ResponseEntity<BookDto.BookListResponse> getBestsellers(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        
        log.info("베스트셀러 조회");
        
        BookDto.BookListResponse response = bookService.getBestsellers(page, size);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "신간 도서 목록",
        description = "최근 출간된 신간 도서 목록을 조회합니다."
    )
    @GetMapping("/new-releases")
    public ResponseEntity<BookDto.BookListResponse> getNewReleases(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        
        log.info("신간 도서 조회");
        
        BookDto.BookListResponse response = bookService.getNewReleases(page, size);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "도서 통계 정보",
        description = "전체 도서 통계 정보를 조회합니다."
    )
    @GetMapping("/statistics")
    public ResponseEntity<BookDto.Statistics> getStatistics() {
        log.info("도서 통계 조회");
        
        BookDto.Statistics statistics = bookService.getStatistics();
        
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "API 상태 확인",
        description = "도서 API 서비스의 상태를 확인합니다."
    )
    @GetMapping("/api/health")
    public ResponseEntity<String> checkApiHealth() {
        log.info("API 상태 확인");
        
        return ResponseEntity.ok("도서 API 서비스가 정상적으로 작동 중입니다.");
    }

    /**
     * 에러 핸들링
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("서버 오류", e);
        return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다.");
    }
} 