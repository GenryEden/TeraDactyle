import pandas as pd
import statistics
import argparse

def read_csv(file_path):
    """
    Reads a CSV file and returns a DataFrame.
    """
    try:
        data = pd.read_csv(file_path)
        return data
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        exit(1)
    except pd.errors.EmptyDataError:
        print(f"Error: File '{file_path}' is empty.")
        exit(1)
    except pd.errors.ParserError:
        print(f"Error: File '{file_path}' is not a valid CSV.")
        exit(1)

def calculate_statistics(data, column_name):
    """
    Calculates mean, median, and mode for a specified column in the DataFrame.
    """
    if column_name not in data.columns:
        print(f"Error: Column '{column_name}' does not exist in the data.")
        exit(1)

    mean_value = data[column_name].mean()
    median_value = data[column_name].median()
    mode_value = data[column_name].mode().iloc[0]  # mode() returns a Series, take the first value
    return mean_value, median_value, mode_value

def main():
    parser = argparse.ArgumentParser(description='Calculate statistics for a specified column in a CSV file.')
    parser.add_argument('file_path', type=str, help='Path to the CSV file')
    parser.add_argument('column_name', type=str, help='Name of the column to analyze')
    args = parser.parse_args()

    # Read the CSV file
    data = read_csv(args.file_path)

    # Calculate statistics
    mean_value, median_value, mode_value = calculate_statistics(data, args.column_name)

    # Print the results
    print(f"Statistics for column '{args.column_name}':")
    print(f"Mean: {mean_value}")
    print(f"Median: {median_value}")
    print(f"Mode: {mode_value}")

if __name__ == "__main__":
    main()
