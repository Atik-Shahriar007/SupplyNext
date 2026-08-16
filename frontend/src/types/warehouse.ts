export interface Warehouse {
  id: number;
  name: string;
  location: string;
  capacity: number;
}
export interface PagedResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}