import pandas as pd
import statistics

def read_csv(file_path):
    """
    Reads a CSV file and returns a DataFrame.
    """
    return pd.read_csv(file_path)

def calculate_statistics(data, column_name):
    """
    Calculates mean, median, and mode for a specified column in the DataFrame.
    """
    mean_value = data[column_name].mean()
    median_value = data[column_name].median()
    mode_value = data[column_name].mode().iloc[0]  # mode() returns a Series, take the first value
    return mean_value, median_value, mode_value

def main():
    # Specify the path to your CSV file
    file_path = 'data.csv'  # Update with your file path
    # Specify the column name for which you want to calculate statistics
    column_name = 'column_name'  # Update with your column name

    # Read the CSV file
    data = read_csv(file_path)

    # Calculate statistics
    mean_value, median_value, mode_value = calculate_statistics(data, column_name)

    # Print the results
    print(f"Statistics for column '{column_name}':")
    print(f"Mean: {mean_value}")
    print(f"Median: {median_value}")
    print(f"Mode: {mode_value}")

if __name__ == "__main__":
    main()
