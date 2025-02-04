import random
import gutenbergpy.textget
import os

# Function to download a book in text format
def download_book(book_id, download_path):
    print(f"Downloading book ID: {book_id}")
    try:
        text = gutenbergpy.textget.get_text_by_id(book_id)
        if text:
            book_path = os.path.join(download_path, f"book_{book_id}.txt")
            with open(book_path, 'wb') as book_file:
                book_file.write(text)
            print(f"Downloaded: {book_path}")
        else:
            print(f"Failed to download book ID: {book_id}")
    except Exception as e:
        print(f"Error downloading book {book_id}: {e}")

# Main function to download random books
def download_random_books(num_books, download_path):
    if not os.path.exists(download_path):
        os.makedirs(download_path)
    
    random_book_ids = random.sample(range(1, 60000), num_books)  # Sample random book IDs
    for book_id in random_book_ids:
        download_book(book_id, download_path)

# Download 10 random books in txt format
download_path = './gutenberg_books'
download_random_books(10, download_path)
