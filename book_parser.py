import pandas as pd

df = pd.read_csv('books.csv', sep="\t")

# Make BOOK table for mysql
book = df[['ISBN10', 'Title']]
book.to_csv('BOOK.csv', index=False, header=False)

# Make AUTHORS table for mysql
authors = df['Author'].str.split(',')
names = []
for author_list in authors:
    if type(author_list) is list:
        for author_name in author_list:
            names.append(author_name)
names = pd.Series(names).drop_duplicates()
authors = {'Author_name': names}
authors = pd.DataFrame(authors)
authors.reset_index(drop=True, inplace=True)
authors.index = authors.index + 1
authors.insert(0, 'Author_id', authors.index)
authors.to_csv('AUTHORS.csv', index=False, header=False)

# Make BOOK_AUTHORS table for mysql
isbns = []
names = []
for index, row in df.iterrows():
    if type(row['Author']) is not float:
        author_list = row['Author'].split(',')
        for author_name in author_list:
            isbns.append(row['ISBN10'])
            names.append(author_name)

book_authors = pd.DataFrame()
book_authors['ISBN10'] = isbns
book_authors['Author_name'] = names
book_authors = pd.merge(authors, book_authors, on='Author_name', how='inner')
book_authors = book_authors.drop('Author_name', axis=1)
book_authors = book_authors.drop_duplicates()
book_authors.to_csv('BOOK_AUTHORS.csv', index=False, header=False)

# Make BORROWERS table for mysql
df = pd.read_csv('borrowers.csv')
borrowers = pd.DataFrame()
borrowers['Card_id'] = df['ID0000id']
borrowers['Ssn'] = df['ssn']
borrowers['Bname'] = df['first_name'] + ' ' + df['last_name']
borrowers['Address'] = df['address'] + ', ' + df['city'] + ', ' + df['state']
borrowers['Phone'] = df['phone']
borrowers.to_csv('BORROWER.csv', index=False, header=False, sep=';')